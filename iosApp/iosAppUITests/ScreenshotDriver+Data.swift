import XCTest

/// The steps that change the app's data: follows through search, update passes, database edits.
extension ScreenshotDriver {
    /// Searches each title and turns its bell on, unless it is on already.
    func follow(_ titles: [String]) throws {
        normalize()
        openTabAnew(listTab)
        for title in titles {
            try search(title)
            let found = waitUntil(loadTimeout) { self.mainCard(title) != nil }
            guard found, let card = mainCard(title), let bell = bell(inside: card.frame) else {
                throw ScreenshotFailure("the search did not show \(title) with its bell")
            }
            if bell.label == turnOnNotifications {
                bell.tap()
                let turnedOn = waitUntil(shortTimeout) { bell.label == turnOffNotifications }
                guard turnedOn else {
                    throw ScreenshotFailure("the bell of \(title) did not turn on")
                }
            }
            expectedFavorites.insert(title)
            closeSearch()
        }
    }

    /// Opens Main's search, types `query` and submits it. No keyboard may cover the results.
    func search(_ query: String) throws {
        if !searchField.exists {
            app.tagged("search_button").tap()
            // A tap that lands while the screen still settles is lost.
            if !searchField.waitForExistence(timeout: shortTimeout) {
                app.tagged("search_button").tap()
            }
        }
        test.waitFor(searchField, timeout: shortTimeout)
        searchField.tap()
        clearSearchField()
        searchField.typeText(query + "\n")
        try hideKeyboard()
    }

    func closeSearch() {
        guard searchField.exists else { return }
        app.tagged("search_button").tap()
        _ = test.waitUntilGone(searchField, timeout: shortTimeout)
    }

    /// One update pass that notifies exactly `titles`; see `AppDatabase.lowerAiredEpisodes`.
    /// Done once every visible named favorite shows "New episode" and the tab counts them all.
    func notify(_ titles: [String]) throws {
        try editDatabase { try $0.lowerAiredEpisodes(of: titles) }
        expectedNewEpisodes = Set(titles)
        openTabAnew(favoritesTab)
        try measureListStart(itemTag: favoriteItemTag)
        pullToRefresh()
        guard !notified(titles, within: firstPassTimeout) else { return }
        // A second pull resets the flags a running pass already raised, so it comes only while
        // no pass has touched the titles.
        if try database().stillLowered(titles) {
            pullToRefresh()
        }
        guard notified(titles, within: loadTimeout) else {
            throw ScreenshotFailure("the update pass did not notify \(titles)")
        }
    }

    func applyEdit(_ edit: DatabaseEdit) throws {
        try editDatabase { try $0.apply(edit) }
        expectedFavorites.subtract(edit.unfollow)
        expectedNewEpisodes.subtract(edit.unfollow)
        if let newEpisodes = edit.newEpisodes {
            expectedNewEpisodes = Set(newEpisodes)
        }
        lastEdit = edit
    }

    /// Ends the app, runs `body` on its database, and starts the app again.
    func editDatabase(_ body: (AppDatabase) throws -> Void) throws {
        let database = try database()
        app.terminate()
        let ended = waitUntil(shortTimeout) { self.app.state == .notRunning }
        guard ended else {
            throw ScreenshotFailure("the app did not end before a database edit")
        }
        try body(database)
        relaunch()
    }

    /// The app's database, found once the app has run.
    func database() throws -> AppDatabase {
        if let foundDatabase {
            return foundDatabase
        }
        let located = try AppDatabase.locate(bundleId: appBundleId)
        foundDatabase = located
        return located
    }

    /// The Favorites tab's label: it counts the favorites with new episodes, when there are any.
    func favoritesLabel(newEpisodes count: Int) -> String {
        count > 0 ? "Favorites, \(count) \(withNewEpisodes)" : "Favorites"
    }

    private func notified(_ titles: [String], within timeout: TimeInterval) -> Bool {
        let label = favoritesLabel(newEpisodes: titles.count)
        return waitUntil(timeout) {
            self.app.tagged(favoritesTab).shows(label) && titles.allSatisfy { title in
                // A favorite below the screen is left to the tab's count.
                self.favoriteCard(title).map { $0.label.contains(newEpisodeText) } ?? true
            }
        }
    }

    /// Pulls Favorites down from its top: the app resets every flag and runs the update pass.
    private func pullToRefresh() {
        let window = app.windows.firstMatch.frame
        let startX = window.minX + window.width * posterColumnFraction - app.frame.minX
        let startY = listTop + dragMargin - app.frame.minY
        let origin = app.coordinate(withNormalizedOffset: .zero)
        let start = origin.withOffset(CGVector(dx: startX, dy: startY))
        let end = origin.withOffset(CGVector(dx: startX, dy: startY + window.height * pullFraction))
        start.press(
            forDuration: pressDuration,
            thenDragTo: end,
            withVelocity: .slow,
            thenHoldForDuration: holdDuration
        )
    }

    private func clearSearchField() {
        let value = searchField.value as? String ?? ""
        guard !value.isEmpty, value != searchHint else { return }
        searchField.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: value.count))
    }

    // Submitting closes the keyboard of a single-line field. A simulator with a hardware keyboard
    // shows none at all.
    private func hideKeyboard() throws {
        let keyboard = app.keyboards.firstMatch
        let closed = waitUntil(shortTimeout) { !keyboard.exists }
        if closed {
            return
        }
        for key in submitKeys where keyboard.buttons[key].exists {
            keyboard.buttons[key].tap()
            break
        }
        let gone = waitUntil(shortTimeout) { !keyboard.exists }
        guard gone else {
            throw ScreenshotFailure("the keyboard stayed over the search results")
        }
    }
}

let turnOnNotifications = "Turn on notifications"
let turnOffNotifications = "Turn off notifications"
let newEpisodeText = "New episode"
let withNewEpisodes = "with new episodes"

/// The search field's placeholder, which an empty field reports as its value.
let searchHint = "Enter the name of anime"

/// The keyboard keys that submit a field, by the names iOS gives them.
let submitKeys = ["search", "Search", "Done", "done", "Return", "return"]

/// How long the first update pass may take before a second pull is considered.
let firstPassTimeout: TimeInterval = 45

/// How far down a pull to refresh drags, as a share of the window's height.
let pullFraction: CGFloat = 0.4
