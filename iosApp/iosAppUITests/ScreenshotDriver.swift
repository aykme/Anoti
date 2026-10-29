import XCTest

/// Runs a screenshot scenario against the real app: composes each screen the steps describe,
/// checks it, and keeps it as attachments the `store-screenshots` job collects. Data steps live in
/// `ScreenshotDriver+Data.swift`, checks and captures in `ScreenshotDriver+Shots.swift`.
@MainActor
final class ScreenshotDriver {
    /// The names of the shots kept so far, in order.
    var keptShots: [String] = []

    let test: XCTestCase
    let app: XCUIApplication

    /// Every title the scenario names, to tell "Cyberpunk: Edgerunners" from its sequel.
    let titles: [String]

    /// The favorites the app should hold, after the follows and removals so far.
    var expectedFavorites: Set<String> = []

    /// The favorites that should show "New episode".
    var expectedNewEpisodes: Set<String> = []

    /// The last database edit, re-applied when a Favorites check fails.
    var lastEdit: DatabaseEdit?

    /// What the next `shoot` checks.
    var screen = Screen.none

    /// Where the first card of the open list sat before any scrolling.
    var listTop: CGFloat = 0
    var listLeft: CGFloat = 0

    var permissionAnswered = false
    var foundDatabase: AppDatabase?

    init(test: XCTestCase, titles: [String]) {
        self.test = test
        self.titles = titles
        app = XCUIApplication()
    }

    /// Launches the app upright and fails when the screenshots would not have `expectedSize`.
    func launch(expectedSize: CGSize) throws {
        XCUIDevice.shared.orientation = .portrait
        relaunch()
        let image = XCUIScreen.main.screenshot().image.cgImage
        let size = CGSize(width: image?.width ?? 0, height: image?.height ?? 0)
        guard size == expectedSize else {
            throw ScreenshotFailure("the screenshots are \(size), not \(expectedSize)")
        }
    }

    func run(_ steps: [ScreenshotStep]) throws {
        for step in steps {
            let handled = try performScreenStep(step)
            if !handled {
                try performDataStep(step)
            }
        }
    }

    // MARK: - Dispatch

    private func performScreenStep(_ step: ScreenshotStep) throws -> Bool {
        switch step {
        case let .onAir(top, dateMode):
            try openList(section: "ongoing_button", top: top, dateMode: dateMode)
        case let .soon(top, dateMode):
            try openList(section: "announced_button", top: top, dateMode: dateMode)
        case let .search(query, dateMode, shows):
            try openSearchResults(for: query, dateMode: dateMode, shows: shows)
        case let .favorites(top, extraInfo):
            try openFavorites(top: top, extraInfo: extraInfo)
        case let .notificationCenter(titles):
            try openNotificationCenter(listing: titles)
        case let .shoot(name):
            try shoot(name)
        case let .turn(orientation):
            turn(to: orientation)
        default:
            return false
        }
        return true
    }

    private func performDataStep(_ step: ScreenshotStep) throws {
        switch step {
        case let .follow(titles):
            try follow(titles)
        case let .notify(titles):
            try notify(titles)
        case let .pause(seconds):
            RunLoop.current.run(until: Date().addingTimeInterval(seconds))
        case let .database(edit):
            try applyEdit(edit)
        default:
            throw ScreenshotFailure("no handler for \(step)")
        }
    }

    // MARK: - Launch and navigation

    /// Starts the app again. The notification question comes once per installation.
    func relaunch() {
        app.launch()
        if !permissionAnswered {
            test.allowNotificationsIfAsked()
            permissionAnswered = true
        }
        test.closeTheDialogIfKept(in: app)
    }

    /// Closes what a relaunch may have kept open: the information dialog and the search field.
    func normalize() {
        test.closeTheDialogIfKept(in: app)
        closeSearch()
    }

    var searchField: XCUIElement {
        app.textViews.firstMatch
    }

    /// Opens the tab the other way round first, so its screen is built anew: a fresh Favorites
    /// turns extra info off, and a fresh Main starts at the top of its list.
    func openTabAnew(_ tab: String) {
        let other = tab == listTab ? favoritesTab : listTab
        test.waitFor(app.tagged(other)).tap()
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
        app.tagged(tab).tap()
        if tab == listTab {
            test.waitFor(app.tagged("ongoing_button"))
        } else {
            test.waitFor(app.allTagged(favoriteItemTag).firstMatch)
        }
    }

    private func openList(section: String, top: Anchor?, dateMode: [String]) throws {
        normalize()
        openTabAnew(listTab)
        app.tagged(section).tap()
        try measureListStart(itemTag: listItemTag)
        if let top {
            try align(to: top, itemTag: listItemTag)
        }
        try showDates(on: dateMode)
        screen = .list(anchor: top, dateMode: dateMode, shows: [])
    }

    private func openSearchResults(for query: String, dateMode: [String], shows: [String]) throws {
        normalize()
        openTabAnew(listTab)
        try search(query)
        let found = waitUntil(loadTimeout) {
            (dateMode + shows).allSatisfy { self.mainCard($0) != nil }
        }
        guard found else {
            throw ScreenshotFailure("search \"\(query)\" did not show \(dateMode + shows)")
        }
        try measureListStart(itemTag: listItemTag)
        try showDates(on: dateMode)
        screen = .list(anchor: nil, dateMode: dateMode, shows: shows)
    }

    func openFavorites(top: Anchor?, extraInfo: [String]) throws {
        normalize()
        openTabAnew(favoritesTab)
        try measureListStart(itemTag: favoriteItemTag)
        if let top {
            try align(to: top, itemTag: favoriteItemTag)
        }
        try turnOnExtraInfo(for: extraInfo)
        screen = .favorites(anchor: top, extraInfo: extraInfo)
    }

    private func turn(to orientation: UIDeviceOrientation) {
        XCUIDevice.shared.orientation = orientation
        let window = app.windows.firstMatch
        let wide = orientation.isLandscape
        // A narrow window stays upright whatever the device does.
        _ = waitUntil(shortTimeout) { (window.frame.width > window.frame.height) == wide }
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
    }

    // MARK: - Finding things

    /// The Main card with this exact title, found by the card's frame around the title text.
    func mainCard(_ title: String) -> XCUIElement? {
        let text = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", title)).firstMatch
        guard text.exists else { return nil }
        let centre = CGPoint(x: text.frame.midX, y: text.frame.midY)
        return app.allTagged(listItemTag).allElementsBoundByIndex
            .first { $0.frame.contains(centre) }
    }

    /// The favorite whose merged label holds this title and no longer title that contains it.
    /// A favorite showing extra info holds no title at all.
    func favoriteCard(_ title: String) -> XCUIElement? {
        let longer = titles.filter { $0 != title && $0.contains(title) }
        return app.allTagged(favoriteItemTag).allElementsBoundByIndex.first { item in
            item.label.contains(title) && !longer.contains { item.label.contains($0) }
        }
    }

    /// The element with this label inside `frame`, a card's part laid out beside it in the tree.
    func element(labelled label: String, inside frame: CGRect) -> XCUIElement? {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", label))
            .allElementsBoundByIndex
            .first { frame.contains(CGPoint(x: $0.frame.midX, y: $0.frame.midY)) }
    }

    /// The bell inside a card's frame.
    func bell(inside frame: CGRect) -> XCUIElement? {
        app.allTagged("notification_button").allElementsBoundByIndex
            .first { frame.contains(CGPoint(x: $0.frame.midX, y: $0.frame.midY)) }
    }

    /// The top of the tab bar: what lies lower is hidden.
    var tabBarTop: CGFloat {
        min(app.tagged(listTab).frame.minY, app.tagged(favoritesTab).frame.minY)
    }

    /// The bottom of Main's top bar, drawn over the list; zero where there is none.
    var topBarBottom: CGFloat {
        let search = app.tagged("search_button")
        return search.exists ? search.frame.maxY : 0
    }

    // MARK: - Scrolling

    func measureListStart(itemTag: String) throws {
        let loaded = waitUntil(loadTimeout) { self.app.allTagged(itemTag).firstMatch.exists }
        guard loaded else {
            throw ScreenshotFailure("the list did not load")
        }
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
        let frames = app.allTagged(itemTag).allElementsBoundByIndex.map { $0.frame }
        listTop = frames.map(\.minY).min() ?? 0
        listLeft = frames.map(\.minX).min() ?? 0
    }

    /// Scrolls until the anchor's card sits within a few points below its target. A long move is
    /// one drag; a short one is a pair, out and back, whose touch slops cancel.
    private func align(to anchor: Anchor, itemTag: String) throws {
        let target = listTop - anchor.raisedBy
        for _ in 0..<maxAlignDrags {
            let card = itemTag == listItemTag ? mainCard(anchor.title) : favoriteCard(anchor.title)
            guard let card else {
                let visible = tabBarTop - max(listTop, topBarBottom)
                drag(by: -visible * revealFraction, itemTag: itemTag)
                continue
            }
            let error = card.frame.minY - target
            if error >= 0, error <= alignTolerance {
                return
            }
            if abs(error) >= minimumDrag {
                drag(by: -error, itemTag: itemTag)
            } else {
                let outward: CGFloat = error > 0 ? -overshoot : overshoot
                drag(by: -error + outward, itemTag: itemTag)
                drag(by: -outward, itemTag: itemTag)
            }
        }
        throw ScreenshotFailure("\(anchor.title) did not settle at the top of the list")
    }

    /// Moves the list's content by `deltaY` points with one press-and-drag: a short press, so no
    /// long press fires, and a hold at the end, so the list does not fling. The drag starts away
    /// from the top bar and, on Favorites, over the posters' column.
    func drag(by deltaY: CGFloat, itemTag: String) {
        let window = app.windows.firstMatch.frame
        let upper = max(listTop, topBarBottom) + dragMargin
        let lower = tabBarTop - dragMargin
        let length = min(abs(deltaY), lower - upper)
        let startY = deltaY < 0 ? lower : upper
        let endY = deltaY < 0 ? startY - length : startY + length
        let startX = itemTag == favoriteItemTag
            ? window.minX + window.width * posterColumnFraction
            : window.midX
        let origin = app.coordinate(withNormalizedOffset: .zero)
        let offsetX = startX - app.frame.minX
        let start = origin.withOffset(CGVector(dx: offsetX, dy: startY - app.frame.minY))
        let end = origin.withOffset(CGVector(dx: offsetX, dy: endY - app.frame.minY))
        start.press(
            forDuration: pressDuration,
            thenDragTo: end,
            withVelocity: .slow,
            thenHoldForDuration: holdDuration
        )
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
    }

    /// Polls `condition` until it holds or `timeout` passes, and tells whether it held.
    func waitUntil(_ timeout: TimeInterval, _ condition: () -> Bool) -> Bool {
        let deadline = Date().addingTimeInterval(timeout)
        while !condition() {
            if Date() >= deadline {
                return false
            }
            RunLoop.current.run(until: Date().addingTimeInterval(pollInterval))
        }
        return true
    }
}

/// What the next `shoot` checks, set by the screen step before it.
enum Screen {
    case none
    case list(anchor: Anchor?, dateMode: [String], shows: [String])
    case favorites(anchor: Anchor?, extraInfo: [String])
    case notificationCenter([String])
}

/// A scenario step that could not reach or prove its screen; the message names what failed.
struct ScreenshotFailure: Error, CustomStringConvertible {
    let description: String

    init(_ description: String) {
        self.description = description
    }
}

let listTab = "anime_list_button"
let favoritesTab = "anime_favorites_button"
let listItemTag = "anime_list_item"
let favoriteItemTag = "anime_favorites_item"

/// How long a short wait lasts: a field closing, a keyboard going, a window turning.
let shortTimeout: TimeInterval = 10

/// How long the screen takes to settle after a tap or a drag.
let settleTime: TimeInterval = 0.6

/// The press before a drag moves: well under the long-press timeout.
let pressDuration: TimeInterval = 0.1

/// The hold at a drag's end that stops the list from flinging.
let holdDuration: TimeInterval = 0.5

/// The shortest drag: longer than any touch slop, so it never counts as a tap.
let minimumDrag: CGFloat = 40

/// How far past the target the first drag of a pair goes.
let overshoot: CGFloat = 60

/// How far below its target an anchor's top may settle.
let alignTolerance: CGFloat = 8

/// How many drags an alignment may take.
let maxAlignDrags = 30

/// How much of the visible list a drag scrolls while the anchor is still out of sight.
let revealFraction: CGFloat = 0.6

/// How far from the list's edges a drag starts and ends.
let dragMargin: CGFloat = 30

/// Where across a favorite a drag starts: over the poster, clear of the counter's arrows.
let posterColumnFraction: CGFloat = 0.15
