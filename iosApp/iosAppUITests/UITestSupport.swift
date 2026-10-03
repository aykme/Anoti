import XCTest

let appBundleId = "com.alekseivinogradov.anoti"

/// How long a screen may take to load from the live backend.
let loadTimeout: TimeInterval = 60

/// The home screen, which shows the system's alerts and notifications.
@MainActor
var springboard: XCUIApplication {
    XCUIApplication(bundleIdentifier: "com.apple.springboard")
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
        if allow.waitForExistence(timeout: 10) {
            allow.tap()
        }
    }
}
