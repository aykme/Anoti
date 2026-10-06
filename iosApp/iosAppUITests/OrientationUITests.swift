import XCTest

/// The app turns only on a wide window, one whose smaller side is at least 600 points. On an
/// iPhone, whose window is narrow, it stays upright. CI runs these when a run sets `ios_tests`, on
/// the devices its `device` input names.
final class OrientationUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testTheAppTurnsOnlyOnAWideWindow() {
        //Given
        let app = launchedUpright()
        // The tests after this one expect an upright device.
        defer { XCUIDevice.shared.orientation = .portrait }
        let wide = isWide(app.windows.firstMatch.frame)

        //When
        XCUIDevice.shared.orientation = .landscapeLeft
        sleep(2)

        //Then
        XCTAssertEqual(XCUIDevice.shared.orientation, .landscapeLeft)
        let frame = app.windows.firstMatch.frame
        if wide {
            XCTAssertGreaterThan(frame.width, frame.height)
        } else {
            XCTAssertGreaterThan(frame.height, frame.width)
        }
        keepScreenshot("turned on its side")
    }

    @MainActor
    func testBothSectionsLayOutAcrossAWideWindow() throws {
        //Given
        let app = launchedUpright()
        try XCTSkipUnless(isWide(app.windows.firstMatch.frame), "a narrow window never turns")
        defer { XCUIDevice.shared.orientation = .portrait }
        XCUIDevice.shared.orientation = .landscapeLeft
        sleep(2)

        //When
        waitFor(app.allTagged("anime_list_item").firstMatch)
        keepScreenshot("the list across a wide window")
        app.tagged("anime_favorites_button").tap()
        sleep(2)
        keepScreenshot("favorites across a wide window")

        //Then
        let window = app.windows.firstMatch.frame
        let listTab = app.tagged("anime_list_button")
        let favoritesTab = app.tagged("anime_favorites_button")
        XCTAssertGreaterThan(window.width, window.height)
        XCTAssertTrue(listTab.isHittable)
        XCTAssertTrue(favoritesTab.isHittable)
        // The bar stretches along the wider edge: one tab on each half.
        XCTAssertLessThan(listTab.frame.midX, window.midX)
        XCTAssertGreaterThan(favoritesTab.frame.midX, window.midX)
        XCTAssertGreaterThan(listTab.frame.minY, window.midY)
    }

    @MainActor
    private func launchedUpright() -> XCUIApplication {
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        allowNotificationsIfAsked()
        waitFor(app.tagged("anime_list_button"))
        return app
    }

    // The app's own threshold: a window whose smaller side reaches 600 points may turn.
    private func isWide(_ frame: CGRect) -> Bool {
        min(frame.width, frame.height) >= 600
    }
}

/// Temporary: sets the largest Display Zoom and text size, then measures the app's window. CI
/// runs it only from its own probe step, which sets ANOTI_MAX_SCALE.
final class MaxScaleProbeUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = true
        let environment = ProcessInfo.processInfo.environment
        try XCTSkipUnless(environment["ANOTI_MAX_SCALE"] == "1", "only the probe step runs this")
    }

    @MainActor
    func testSetLargerTextDisplayZoom() {
        //Given
        let settings = XCUIApplication(bundleIdentifier: "com.apple.Preferences")
        settings.launch()
        sleep(3)
        keepScreenshot("settings opened")

        //When
        tapLabelled("Display & Brightness", in: settings)
        tapLabelled("Display Zoom", in: settings)
        tapLabelled("Larger Text", in: settings)
        for label in ["Done", "Set"] where settings.buttons[label].exists {
            settings.buttons[label].tap()
            sleep(2)
            keepScreenshot("after \(label)")
        }
        let confirm = settings.descendants(matching: .button)
            .matching(NSPredicate(format: "label BEGINSWITH %@", "Use")).firstMatch
        if confirm.waitForExistence(timeout: 5) {
            confirm.tap()
        }

        //Then
        sleep(5)
        keepScreenshot("after the zoom")
        keepText("settings after the zoom", settings.debugDescription)
    }

    @MainActor
    func testMeasureTheAppAtMaxScale() {
        //Given
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launch()
        allowNotificationsIfAsked()
        waitFor(app.tagged("anime_list_button"))
        sleep(3)
        defer { XCUIDevice.shared.orientation = .portrait }

        //When
        let upright = app.windows.firstMatch.frame
        let screen = XCUIScreen.main.screenshot().image
        keepText("upright window", "\(upright); screen \(screen.size) at scale \(screen.scale)")
        keepScreenshot("the list upright")
        XCUIDevice.shared.orientation = .landscapeLeft
        sleep(3)
        let turned = app.windows.firstMatch.frame
        keepText("turned window", "\(turned), wider than tall: \(turned.width > turned.height)")
        keepScreenshot("the list turned")
        app.tagged("anime_favorites_button").tap()
        sleep(3)
        keepScreenshot("favorites turned")

        //Then
        XCTAssertGreaterThan(upright.width, 0)
    }

    @MainActor
    private func tapLabelled(_ text: String, in app: XCUIApplication) {
        let element = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", text)).firstMatch
        guard element.waitForExistence(timeout: 10) else {
            keepText("no \(text)", app.debugDescription)
            XCTFail("\(text) is not on screen")
            return
        }
        element.tap()
        sleep(2)
        keepScreenshot("after \(text)")
    }

    private func keepText(_ name: String, _ text: String) {
        let attachment = XCTAttachment(string: text)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
