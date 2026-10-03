import XCTest

/// Runs the shipped app end to end against the live Shikimori backend: real graph, real
/// network, real screens. Reaching the network is the point, and the one deliberate exception
/// to the project's rule against it, as in Android's AnimeFavoritesUserFlowTest. The assertions
/// stay on structure, never on values the backend decides.
final class AnimeFavoritesUserFlowUITests: XCTestCase {

    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testAddOngoingToAnimeFavorites() {
        //Given
        let app = launchedApp()
        let listItem = firstOngoing(in: app)
        XCTAssertTrue(listItem.tagged("notification_button").label == "Turn on notifications")

        //When
        listItem.tagged("notification_button").tap()
        waitForBell(of: listItem, label: "Turn off notifications")
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
        let listItem = firstOngoing(in: app)
        listItem.tagged("notification_button").tap()
        waitForBell(of: listItem, label: "Turn off notifications")
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

    @MainActor
    private func firstOngoing(in app: XCUIApplication) -> XCUIElement {
        waitFor(app.tagged("ongoing_button")).tap()
        return waitFor(app.allTagged("anime_list_item").firstMatch)
    }

    @MainActor
    private func waitForBell(of item: XCUIElement, label: String) {
        let bell = item.tagged("notification_button")
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

    // The favorites item merges its children, so its bell may only be reachable from the screen.
    @MainActor
    private func removeTheOnlyFavorite(in app: XCUIApplication) {
        let favorite = app.allTagged("anime_favorites_item").firstMatch
        let bellInside = favorite.tagged("notification_button")
        let bell = bellInside.exists ? bellInside : app.tagged("notification_button")
        bell.tap()
        let emptyText = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", "You haven")).firstMatch
        waitFor(emptyText, timeout: 10)
    }
}
