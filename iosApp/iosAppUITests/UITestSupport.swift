import XCTest

let appBundleId = "com.alekseivinogradov.anoti"

/// How long a screen may take to load from the live backend.
let loadTimeout: TimeInterval = 60

/// How long the system may take to ask for permission to notify.
let notificationQuestionTimeout: TimeInterval = 10

/// How long the information dialog may take to close.
let dialogCloseTimeout: TimeInterval = 10

/// How often a wait for one of several elements looks again.
let pollInterval: TimeInterval = 0.5

/// The home screen, which shows the system's alerts and notifications.
@MainActor
var springboard: XCUIApplication {
    XCUIApplication(bundleIdentifier: "com.apple.springboard")
}

/// The app's own threshold: a window whose smaller side reaches 600 points may turn.
let wideWindowMinSide: CGFloat = 600

/// Whether a window with this frame is wide enough to turn.
func isWide(_ frame: CGRect) -> Bool {
    min(frame.width, frame.height) >= wideWindowMinSide
}

extension XCUIElement {
    /// The first element under this one whose test tag is `tag`.
    func tagged(_ tag: String) -> XCUIElement {
        descendants(matching: .any).matching(identifier: tag).firstMatch
    }

    /// Every element under this one whose test tag is `tag`.
    func allTagged(_ tag: String) -> XCUIElementQuery {
        descendants(matching: .any).matching(identifier: tag)
    }

    /// Whether this element, or one under it, carries `text`. A Compose element that merges
    /// its children holds their texts in its own label.
    func shows(_ text: String) -> Bool {
        let child = descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", text)).firstMatch
        return child.exists || label.contains(text)
    }
}

extension XCTestCase {
    /// Waits for `element` and fails the test when it does not come.
    @MainActor
    @discardableResult
    func waitFor(
        _ element: XCUIElement,
        timeout: TimeInterval = loadTimeout,
        file: StaticString = #filePath,
        line: UInt = #line
    ) -> XCUIElement {
        XCTAssertTrue(
            element.waitForExistence(timeout: timeout),
            "\(element) did not appear",
            file: file,
            line: line
        )
        return element
    }

    /// Waits until `element` is gone, and tells whether it went within `timeout`.
    @MainActor
    func waitUntilGone(_ element: XCUIElement, timeout: TimeInterval) -> Bool {
        let gone = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == false"),
            object: element
        )
        return XCTWaiter().wait(for: [gone], timeout: timeout) == .completed
    }

    /// Keeps a screenshot of the whole screen in the result bundle.
    @MainActor
    func keepScreenshot(_ name: String) {
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    /// Taps Allow on the notification question when the system shows it.
    @MainActor
    func allowNotificationsIfAsked() {
        let allow = springboard.alerts.buttons["Allow"]
        if allow.waitForExistence(timeout: notificationQuestionTimeout) {
            allow.tap()
        }
    }

    /// Closes the information dialog when the app opens with it. The UI tests share one
    /// installation, and a test that sends the app to Safari leaves the dialog kept open.
    @MainActor
    func closeTheDialogIfKept(in app: XCUIApplication) {
        let title = app.tagged("info_dialog_title")
        // The open dialog hides the bar from the accessibility tree, so the first screen shows
        // one of the two.
        let listTab = app.tagged("anime_list_button")
        let deadline = Date().addingTimeInterval(loadTimeout)
        while !title.exists, !listTab.exists, Date() < deadline {
            RunLoop.current.run(until: Date().addingTimeInterval(pollInterval))
        }
        guard title.exists else { return }
        app.tagged("info_dialog_close").tap()
        XCTAssertTrue(
            waitUntilGone(title, timeout: dialogCloseTimeout),
            "the kept dialog did not close"
        )
    }
}
