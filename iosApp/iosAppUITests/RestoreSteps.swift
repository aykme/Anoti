import XCTest

/// Steps the restore script on CI runs one at a time, between its own simulator commands. They
/// are not tests of their own and skip themselves in a plain test run.
///
/// The script starts the app, so its log is captured. A step attaches to that app with
/// activate() and never launches it. Starting the test runner sends the app to the background,
/// and activate() brings it back.
final class RestoreSteps: XCTestCase {

    private let environment = ProcessInfo.processInfo.environment

    @MainActor
    private var app: XCUIApplication {
        XCUIApplication(bundleIdentifier: appBundleId)
    }

    override func setUpWithError() throws {
        continueAfterFailure = false
        try XCTSkipUnless(
            environment["ANOTI_RESTORE_STEPS"] == "1",
            "run only by the restore script"
        )
    }

    @MainActor
    func testLeaveTheAppOnFavoritesAfterASearch() {
        //Given
        bringTheAppForward()
        waitFor(app.tagged("search_button")).tap()
        let field = waitFor(app.textFields.firstMatch, timeout: 10)
        field.tap()
        field.typeText("Frieren\n")

        //When
        waitFor(app.tagged("anime_favorites_button"), timeout: 10).tap()
        waitForFavorites()
        keepScreenshot("favorites before going home")
        XCUIDevice.shared.press(.home)

        //Then
        assertInTheBackground()
    }

    @MainActor
    func testLeaveTheAppOnTheList() {
        //Given
        bringTheAppForward()

        //When
        waitFor(app.tagged("anime_list_button")).tap()
        waitFor(app.allTagged("anime_list_item").firstMatch)
        XCUIDevice.shared.press(.home)

        //Then
        assertInTheBackground()
    }

    @MainActor
    func testTapTheNotificationFromTheBackground() {
        //Given
        bringTheAppForward()
        waitFor(app.tagged("anime_list_button")).tap()
        waitFor(app.allTagged("anime_list_item").firstMatch)
        XCUIDevice.shared.press(.home)
        signalReady()

        //When
        tapTheNotification(timeout: 150)

        //Then
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 30))
        waitForFavorites()
        keepScreenshot("favorites after a tap from the background")
    }

    @MainActor
    func testTapTheNotificationWithTheAppClosed() {
        //Given
        XCTAssertTrue(app.state == .notRunning || app.state == .unknown)

        //When
        tapTheNotification(timeout: 30)

        //Then
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 60))
        waitForFavorites()
        keepScreenshot("favorites after a cold tap")
    }

    @MainActor
    func testAnswerTheNotificationQuestion() {
        //Given
        bringTheAppForward()
        let allow = springboard.alerts.buttons["Allow"]
        XCTAssertTrue(allow.waitForExistence(timeout: 30))

        //When
        allow.tap()

        //Then
        let gone = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == false"),
            object: allow
        )
        XCTAssertEqual(XCTWaiter().wait(for: [gone], timeout: 5), .completed)
    }

    // No API opens the switcher: the gesture is a swipe up from the bottom edge that holds
    // halfway.
    @MainActor
    func testShowTheAppSwitcher() {
        //Given
        bringTheAppForward()
        waitFor(app.tagged("anime_list_button"))
        XCUIDevice.shared.press(.home)
        sleep(1)

        //When
        let bottom = springboard.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.998))
        let middle = springboard.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.55))
        bottom.press(
            forDuration: 0.1,
            thenDragTo: middle,
            withVelocity: .slow,
            thenHoldForDuration: 1.5
        )
        sleep(2)

        //Then
        keepScreenshot("app switcher")
        assertInTheBackground()
    }

    @MainActor
    private func bringTheAppForward() {
        XCUIDevice.shared.orientation = .portrait
        app.activate()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 30))
    }

    // iOS suspends a backgrounded app within seconds; both states mean it left the screen.
    @MainActor
    private func assertInTheBackground() {
        let left: [XCUIApplication.State] = [.runningBackground, .runningBackgroundSuspended]
        let deadline = Date().addingTimeInterval(10)
        while !left.contains(app.state) && Date() < deadline {
            RunLoop.current.run(until: Date().addingTimeInterval(0.5))
        }
        XCTAssertTrue(left.contains(app.state), "the app is still in state \(app.state.rawValue)")
    }

    // Tells the script the app sits in the background, so it can push now. The simulator's
    // processes share the runner's file system.
    private func signalReady() {
        guard let path = environment["ANOTI_READY_FILE"], !path.isEmpty else { return }
        FileManager.default.createFile(atPath: path, contents: Data())
    }

    // A banner is tapped while it shows; after that the notification list is opened.
    @MainActor
    private func tapTheNotification(timeout: TimeInterval) {
        let notification = springboard.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", "Anoti CI notification"))
            .firstMatch
        if !notification.waitForExistence(timeout: timeout) {
            let top = springboard.coordinate(withNormalizedOffset: CGVector(dx: 0.3, dy: 0.01))
            let down = springboard.coordinate(withNormalizedOffset: CGVector(dx: 0.3, dy: 0.6))
            top.press(forDuration: 0.1, thenDragTo: down)
        }
        waitFor(notification, timeout: 20).tap()
    }

    @MainActor
    private func waitForFavorites() {
        let emptyText = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", "You haven")).firstMatch
        waitFor(emptyText)
    }
}
