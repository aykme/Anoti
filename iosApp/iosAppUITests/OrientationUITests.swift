import XCTest

/// The app turns only on a wide window, one whose smaller side is at least 600 points. CI runs
/// these on an iPhone, whose window is narrow. On an iPad, whose window is wide, they run when a
/// run asks for it.
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
