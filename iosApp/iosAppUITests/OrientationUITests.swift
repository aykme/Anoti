import XCTest

/// The app turns only on a wide window. The CI simulator is an iPhone, whose window is narrow.
final class OrientationUITests: XCTestCase {

    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testANarrowIPhoneKeepsTheAppUpright() {
        //Given
        let app = XCUIApplication()
        app.launch()
        allowNotificationsIfAsked()
        waitFor(app.tagged("anime_list_button"))
        // The tests after this one expect an upright device.
        defer { XCUIDevice.shared.orientation = .portrait }

        //When
        XCUIDevice.shared.orientation = .landscapeLeft
        sleep(2)

        //Then
        XCTAssertEqual(XCUIDevice.shared.orientation, .landscapeLeft)
        let frame = app.windows.firstMatch.frame
        XCTAssertGreaterThan(frame.height, frame.width)
        keepScreenshot("iPhone turned on its side")
    }
}
