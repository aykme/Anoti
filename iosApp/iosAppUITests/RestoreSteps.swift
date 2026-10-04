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
        // Compose shows the search input to the accessibility tree as a text view.
        let field = app.textViews.firstMatch
        waitFor(app.tagged("search_button")).tap()
        // A tap that lands while the screen still settles is lost.
        if !field.waitForExistence(timeout: 10) {
            app.tagged("search_button").tap()
        }
        waitFor(field, timeout: 10)
        field.tap()
        field.typeText("Frieren\n")

        //When
        waitFor(app.tagged("anime_favorites_button"), timeout: 10).tap()
        waitForFavorites()
        keepScreenshot("favorites before going home")
        signalReady()
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
        waitForTheList()
        // The launch before kept the list on ongoing. A state equal to the kept one is not written
        // again, and the script waits for a write.
        app.tagged("announced_button").tap()
        signalReady()
        XCUIDevice.shared.press(.home)

        //Then
        assertInTheBackground()
    }

    @MainActor
    func testTapTheNotificationFromTheBackground() {
        //Given
        bringTheAppForward()
        waitFor(app.tagged("anime_list_button")).tap()
        waitForTheList()
        XCUIDevice.shared.press(.home)
        // A notification that arrives while the app is still in front shows no banner.
        assertInTheBackground()
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
        signalReady()

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
        // A tap that lands while the alert still animates in is lost.
        if !waitUntilGone(allow, timeout: 5) {
            allow.tap()
        }

        //Then
        XCTAssertTrue(waitUntilGone(allow, timeout: 10))
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
        // The switcher check must fail on this one, or it could not tell the two apart.
        keepScreenshot("home screen")

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
        // The app may count as in front while the switcher shows it, so its state proves nothing.
        // The CI script judges this screenshot by the card's dark bars, and the home screen above
        // by their absence.
        keepScreenshot("app switcher")
    }

    @MainActor
    private func waitUntilGone(_ element: XCUIElement, timeout: TimeInterval) -> Bool {
        let gone = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == false"),
            object: element
        )
        return XCTWaiter().wait(for: [gone], timeout: timeout) == .completed
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

    // Tells the script the step reached its moment: ready for the notification, or about to go
    // home. The simulator's processes share the runner's file system.
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

    // The list screen is open once its top bar is. Its items come from the live backend, which a
    // step does not need.
    @MainActor
    private func waitForTheList() {
        waitFor(app.tagged("ongoing_button"))
    }

    // Either way favorites is open: empty, or holding a favorite a failed UI test left behind.
    @MainActor
    private func waitForFavorites() {
        let emptyText = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", "You haven")).firstMatch
        let favorite = app.allTagged("anime_favorites_item").firstMatch
        let deadline = Date().addingTimeInterval(loadTimeout)
        while !emptyText.exists && !favorite.exists && Date() < deadline {
            RunLoop.current.run(until: Date().addingTimeInterval(0.5))
        }
        XCTAssertTrue(emptyText.exists || favorite.exists, "favorites did not open")
    }
}
