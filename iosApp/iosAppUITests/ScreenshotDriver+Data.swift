import Foundation
import XCUIAutomation

/// The steps that change the app's data: follows through search, update passes, database edits.
extension ScreenshotDriver {
    /// Searches each title and turns its bell on, unless it is on already.
    func follow(_ titles: [String]) throws {
        try normalize()
        try openTabAnew(listTab)
        for title in titles {
            try search(title)
            let found = waitForResults { self.titleBell(title) != nil }
            guard found, let button = titleBell(title) else {
                throw ScreenshotFailure("the search did not show \(title) with its bell")
            }
            if button.label == turnOnNotifications {
                button.tap()
                let turnedOn = waitUntil(shortTimeout) {
                    self.titleBell(title)?.label == turnOffNotifications
                }
                guard turnedOn else {
                    throw ScreenshotFailure("the bell of \(title) did not turn on")
                }
            }
            expectedFavorites.insert(title)
            try closeSearch()
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
        try clearSearchField()
        searchField.typeText(query + "\n")
        try hideKeyboard()
        // Results arrive in more than one update while the query settles.
        RunLoop.current.run(until: Date().addingTimeInterval(searchSettleTime))
    }

    /// Closes Main's search field, whose close button takes the place of the top bar's tabs.
    func closeSearch() throws {
        guard searchField.exists else { return }
        let close = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", closeSearchLabel)).firstMatch
        test.waitFor(close, timeout: shortTimeout).tap()
        guard test.waitUntilGone(searchField, timeout: shortTimeout) else {
            throw ScreenshotFailure("the search field stayed open")
        }
    }

    // Found anew on every read: the results can reorder while the query settles.
    private func titleBell(_ title: String) -> XCUIElement? {
        mainCard(title).flatMap { card in bell(inside: card.frame) }
    }

    /// One update pass that notifies exactly `titles`; see `AppDatabase.lowerAiredEpisodes`.
    /// Done once every visible named favorite shows "New episode" and the tab counts them all.
    func notify(_ titles: [String]) throws {
        try editDatabase { try $0.lowerAiredEpisodes(of: titles) }
        expectedNewEpisodes = Set(titles)
        try openTabAnew(favoritesTab)
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
        let store = try database()
        app.terminate()
        let ended = waitUntil(shortTimeout) { self.app.state == .notRunning }
        guard ended else {
            throw ScreenshotFailure("the app did not end before a database edit")
        }
        try body(store)
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

    // The field keeps its text when closed. A tap puts the caret where it lands and a backspace
    // deletes only what lies before it, so each round taps right of the remaining text, left of
    // the close button.
    private func clearSearchField() throws {
        let caret = searchField.coordinate(withNormalizedOffset: caretOffset)
        for _ in 0..<clearRounds {
            caret.tap()
            let value = searchField.value as? String ?? ""
            if value.isEmpty || value == searchHint {
                return
            }
            searchField.typeText(
                String(repeating: XCUIKeyboardKey.delete.rawValue, count: value.count)
            )
        }
        let left = searchField.value as? String ?? ""
        guard left.isEmpty || left == searchHint else {
            throw ScreenshotFailure("the search field kept \"\(left)\"")
        }
    }

    // Submitting closes the keyboard of a single-line field. A simulator with a hardware keyboard
    // shows none at all. A fresh simulator's first keyboard can swallow the submit under its
    // typing tip, so the submit key is pressed again.
    private func hideKeyboard() throws {
        let keyboard = app.keyboards.firstMatch
        var closed = waitUntil(shortTimeout) { !keyboard.exists }
        for _ in 0..<submitPresses where !closed {
            if let key = submitKeys.first(where: { keyboard.buttons[$0].exists }) {
                keyboard.buttons[key].tap()
            }
            closed = waitUntil(shortTimeout) { !keyboard.exists }
        }
        guard closed else {
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

/// The label of the button that closes the search field.
let closeSearchLabel = "Close search input field"

/// Where in the search field a tap puts the caret: right of short text, left of the close button.
let caretOffset = CGVector(dx: caretX, dy: caretY)
let caretX: CGFloat = 0.7
let caretY: CGFloat = 0.5

/// How many tap-and-delete rounds clearing the search field may take.
let clearRounds = 4

/// How long search results take to settle after the query is submitted.
let searchSettleTime: TimeInterval = 2

/// The keyboard keys that submit a field, by the names iOS gives them.
let submitKeys = ["search", "Search", "Done", "done", "Return", "return"]

/// How many times the submit key is pressed while the keyboard stays.
let submitPresses = 3

/// How long the first update pass may take before a second pull is considered.
let firstPassTimeout: TimeInterval = 45

/// How far down a pull to refresh drags, as a share of the window's height.
let pullFraction: CGFloat = 0.4
