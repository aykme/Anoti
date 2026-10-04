import XCTest

/// Runs the real app end to end against the live Shikimori backend: real graph, real
/// network, real screens. Reaching the network is the point. Such a test is the project's last
/// resort, written with the developer's permission, as Android's test of the same name is.
/// The assertions stay on structure, never on values the backend decides.
final class AnimeFavoritesUserFlowTest: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testAddOngoingToAnimeFavorites() {
        //Given
        let app = launchedApp()
        let bell = firstOngoingBell(in: app)
        XCTAssertEqual(bell.label, "Turn on notifications")

        //When
        bell.tap()
        waitFor(bell, toRead: "Turn off notifications")
        app.tagged("anime_favorites_button").tap()

        //Then
        let favorite = waitFor(app.allTagged("anime_favorites_item").firstMatch)
        XCTAssertTrue(onlyFavoriteShows("The image of the rating in the form of a star", in: app))
        XCTAssertTrue(onlyFavoriteShows("Turn on the display of extra information", in: app))
        XCTAssertTrue(onlyFavoriteShows("Ongoing", in: app))
        XCTAssertTrue(onlyFavoriteShows("Turn off notifications", in: app))
        // Android checks a text starting with "Episodes", followed by the backend's count.
        let episodes = NSPredicate(format: "label BEGINSWITH %@", "Episodes")
        XCTAssertTrue(
            favorite.label.contains("Episodes")
                || app.descendants(matching: .any).matching(episodes).firstMatch.exists
        )
        keepScreenshot("favorites with the added ongoing")

        // Leaves favorites empty, as the next test and the restore checks expect.
        removeTheOnlyFavorite(in: app)
    }

    @MainActor
    func testRemoveOngoingFromAnimeFavorites() {
        //Given
        let app = launchedApp()
        let bell = firstOngoingBell(in: app)
        bell.tap()
        waitFor(bell, toRead: "Turn off notifications")
        app.tagged("anime_favorites_button").tap()
        waitFor(app.allTagged("anime_favorites_item").firstMatch)

        //When
        removeTheOnlyFavorite(in: app)

        //Then
        XCTAssertEqual(app.allTagged("anime_favorites_item").count, 0)
        keepScreenshot("favorites empty again")
    }

    @MainActor
    private func launchedApp() -> XCUIApplication {
        let app = XCUIApplication()
        app.launch()
        allowNotificationsIfAsked()
        return app
    }

    // Compose lays an item's parts out beside the element its test tag names, not under it. The
    // first bell on the screen is the first item's. The app may open on favorites, kept from an
    // earlier run.
    @MainActor
    private func firstOngoingBell(in app: XCUIApplication) -> XCUIElement {
        waitFor(app.tagged("anime_list_button")).tap()
        waitFor(app.tagged("ongoing_button")).tap()
        waitFor(app.allTagged("anime_list_item").firstMatch)
        return waitFor(app.allTagged("notification_button").firstMatch)
    }

    @MainActor
    private func waitFor(_ bell: XCUIElement, toRead label: String) {
        let changed = NSPredicate(format: "label == %@", label)
        let expectation = XCTNSPredicateExpectation(predicate: changed, object: bell)
        XCTAssertEqual(XCTWaiter().wait(for: [expectation], timeout: 10), .completed)
    }

    // The screen holds one favorite, so a text anywhere on it belongs to that one. The item may
    // merge some children into its own label and leave its buttons as elements of their own.
    @MainActor
    private func onlyFavoriteShows(_ text: String, in app: XCUIApplication) -> Bool {
        let favorite = app.allTagged("anime_favorites_item").firstMatch
        let anywhere = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", text)).firstMatch
        return favorite.shows(text) || anywhere.exists
    }

    // The screen holds one favorite, so the one bell on it is that favorite's.
    @MainActor
    private func removeTheOnlyFavorite(in app: XCUIApplication) {
        app.allTagged("notification_button").firstMatch.tap()
        let emptyText = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", "You haven")).firstMatch
        waitFor(emptyText, timeout: 10)
    }
}
