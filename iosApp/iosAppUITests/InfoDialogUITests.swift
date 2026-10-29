import XCTest

/// The information dialog in the real app: Close, an outside tap, each link and a wide window on
/// its side. CI runs these when a run sets `ios_tests`, on the devices its `device` names.
final class InfoDialogUITests: XCTestCase {
    /// How long Safari may take to come to the front with a page.
    private let safariTimeout: TimeInterval = 30

    /// How long the app may take to come back to the front.
    private let foregroundTimeout: TimeInterval = 30

    /// How long the dialog may take to open or close. Safari's first launch can slow the runner
    /// down for a while.
    private let dialogTimeout: TimeInterval = 30

    /// The smallest gap an outside tap aims at: Apple's minimum touch target.
    private let minimumGap: CGFloat = 44

    /// The dialog's padding around its title, links and Close.
    private let dialogPadding: CGFloat = 24

    @MainActor
    private var safari: XCUIApplication {
        XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
    }

    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testCloseClosesTheDialog() {
        //Given
        let app = launchedOnTheList()
        let title = openTheDialog(in: app)
        keepScreenshot("the information dialog")

        //When
        app.tagged("info_dialog_close").tap()

        //Then
        XCTAssertTrue(waitUntilGone(title, timeout: dialogTimeout), "Close left the dialog open")
        // Safari was ended before the launch, so a running one is one Close opened. A Safari that
        // has not run since may report its state as unknown.
        let notLaunched: [XCUIApplication.State] = [.notRunning, .unknown]
        XCTAssertTrue(notLaunched.contains(safari.state), "Close opened Safari")
        let infoButton = app.tagged("info_button")
        XCTAssertTrue(infoButton.isHittable)
        infoButton.tap()
        waitFor(title, timeout: dialogTimeout)
    }

    @MainActor
    func testPrivacyPolicyOpensSafariAndKeepsTheDialog() {
        //Given
        let app = launchedOnTheList()
        openTheDialog(in: app)

        //When
        app.tagged("info_dialog_privacy_policy").tap()

        //Then
        assertSafariOpenedAndTheDialogStayed(in: app)
    }

    @MainActor
    func testSupportOpensSafariAndKeepsTheDialog() {
        //Given
        let app = launchedOnTheList()
        openTheDialog(in: app)

        //When
        app.tagged("info_dialog_support").tap()

        //Then
        assertSafariOpenedAndTheDialogStayed(in: app)
    }

    @MainActor
    func testATapOutsideClosesTheDialog() throws {
        //Given
        let app = launchedOnTheList()
        // Read before the dialog opens, since its scrim may hide the screen from the
        // accessibility tree. The bars bound the gaps above and below the dialog, so a tap never
        // lands in the status bar.
        let top = app.tagged("info_button").frame.minY
        let bottom = app.tagged("anime_list_button").frame.maxY
        let window = app.windows.firstMatch.frame
        let title = openTheDialog(in: app)
        keepScreenshot("the information dialog before a tap outside")
        let gaps = gapsAround(dialogFrame(in: app), top: top, bottom: bottom, window: window)
        let widest = try XCTUnwrap(gaps.max { $0.across < $1.across })
        XCTAssertGreaterThanOrEqual(
            widest.across,
            minimumGap,
            "no gap around the dialog fits a tap: "
                + gaps.map(\.description).joined(separator: ", ")
        )

        //When
        let origin = app.frame.origin
        let offset = CGVector(dx: widest.middle.x - origin.x, dy: widest.middle.y - origin.y)
        app.coordinate(withNormalizedOffset: .zero).withOffset(offset).tap()

        //Then
        XCTAssertTrue(
            waitUntilGone(title, timeout: dialogTimeout),
            "a tap outside left the dialog open"
        )
    }

    @MainActor
    func testTheDialogFitsAWideWindowOnItsSide() throws {
        //Given
        let app = launchedOnTheList()
        try XCTSkipUnless(isWide(app.windows.firstMatch.frame), "a narrow window never turns")
        // The tests after this one expect an upright device.
        defer { XCUIDevice.shared.orientation = .portrait }
        XCUIDevice.shared.orientation = .landscapeLeft
        sleep(2)

        //When
        let title = openTheDialog(in: app)
        keepScreenshot("the information dialog across a wide window")

        //Then
        let window = app.windows.firstMatch.frame
        let close = app.tagged("info_dialog_close")
        XCTAssertGreaterThan(window.width, window.height)
        XCTAssertTrue(title.isHittable)
        XCTAssertTrue(close.isHittable)
        XCTAssertTrue(app.tagged("info_dialog_privacy_policy").exists)
        XCTAssertTrue(app.tagged("info_dialog_support").exists)
        close.tap()
        XCTAssertTrue(waitUntilGone(title, timeout: dialogTimeout), "Close left the dialog open")
    }

    // Safari is ended first, so the one a link brings to the front is new.
    @MainActor
    private func launchedOnTheList() -> XCUIApplication {
        XCUIDevice.shared.orientation = .portrait
        safari.terminate()
        let app = XCUIApplication()
        app.launch()
        allowNotificationsIfAsked()
        closeTheDialogIfKept(in: app)
        // The app may open on favorites, kept from an earlier run.
        waitFor(app.tagged("anime_list_button")).tap()
        waitFor(app.tagged("info_button"))
        return app
    }

    // A tap that lands while the screen still settles is lost.
    @MainActor
    @discardableResult
    private func openTheDialog(in app: XCUIApplication) -> XCUIElement {
        let title = app.tagged("info_dialog_title")
        app.tagged("info_button").tap()
        if !title.waitForExistence(timeout: dialogTimeout) {
            app.tagged("info_button").tap()
        }
        return waitFor(title, timeout: dialogTimeout)
    }

    // The page itself comes from the network, so only Safari coming to the front is checked.
    @MainActor
    private func assertSafariOpenedAndTheDialogStayed(in app: XCUIApplication) {
        XCTAssertTrue(
            safari.wait(for: .runningForeground, timeout: safariTimeout),
            "Safari did not come to the front"
        )
        app.activate()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: foregroundTimeout))
        let title = waitFor(app.tagged("info_dialog_title"), timeout: dialogTimeout)
        keepScreenshot("the information dialog after the browser")
        app.tagged("info_dialog_close").tap()
        XCTAssertTrue(waitUntilGone(title, timeout: dialogTimeout), "Close left the dialog open")
    }

    // A container that carries only a test tag may stay out of the accessibility tree. Then the
    // dialog's own elements, grown by its padding, stand in for it.
    @MainActor
    private func dialogFrame(in app: XCUIApplication) -> CGRect {
        let dialog = app.tagged("info_dialog")
        if dialog.exists, !dialog.frame.isEmpty {
            return dialog.frame
        }
        let parts = [
            "info_dialog_title",
            "info_dialog_privacy_policy",
            "info_dialog_support",
            "info_dialog_close"
        ]
        var frame = CGRect.null
        for part in parts {
            frame = frame.union(app.tagged(part).frame)
        }
        return frame.insetBy(dx: -dialogPadding, dy: -dialogPadding)
    }

    // Above and below reach the bars' bounds; left and right reach the window's edges at the
    // dialog's height.
    private func gapsAround(
        _ dialog: CGRect,
        top: CGFloat,
        bottom: CGFloat,
        window: CGRect
    ) -> [Gap] {
        [
            Gap(
                side: "above",
                across: dialog.minY - top,
                middle: CGPoint(x: dialog.midX, y: (top + dialog.minY) / 2)
            ),
            Gap(
                side: "below",
                across: bottom - dialog.maxY,
                middle: CGPoint(x: dialog.midX, y: (dialog.maxY + bottom) / 2)
            ),
            Gap(
                side: "left",
                across: dialog.minX - window.minX,
                middle: CGPoint(x: (window.minX + dialog.minX) / 2, y: dialog.midY)
            ),
            Gap(
                side: "right",
                across: window.maxX - dialog.maxX,
                middle: CGPoint(x: (dialog.maxX + window.maxX) / 2, y: dialog.midY)
            )
        ]
    }
}

/// A strip of the screen between the dialog and what bounds it on one side.
private struct Gap: CustomStringConvertible {
    /// Where the strip lies: above, below, left or right of the dialog.
    let side: String

    /// How much room the strip gives a tap, from the dialog to its bound.
    let across: CGFloat

    /// The point in the middle of the strip, where a tap aims.
    let middle: CGPoint

    var description: String { "\(side) \(across) pt" }
}
