import XCTest

/// Carries out a screenshot scenario in the real app. The scenario says what each screen shows;
/// the driver knows how to get there: searches, taps, scrolls, database edits and Notification
/// Center. So a new set of screenshots changes the scenario, not this code. Each screen is checked
/// before it is kept, so a wrong one fails the run instead of reaching the store.
///
/// Data steps live in `ScreenshotDriver+Data.swift`, finding and scrolling in
/// `ScreenshotDriver+Lists.swift`, checks and captures in `ScreenshotDriver+Shots.swift`, and
/// Notification Center in `ScreenshotDriver+NotificationCenter.swift`.
@MainActor
final class ScreenshotDriver {
    /// The names of the shots kept so far, in order.
    var keptShots: [String] = []

    let test: XCTestCase
    let app: XCUIApplication

    /// Whether the scenario runs on an iPad, whose list has columns and turns on its side.
    let isPad: Bool

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
        isPad = UIDevice.current.userInterfaceIdiom == .pad
    }

    /// Launches the app upright and fails when the screenshots would not have `expectedSize`.
    func launch(expectedSize: CGSize) throws {
        XCUIDevice.shared.orientation = .portrait
        if isPad {
            useFullScreenApps()
        }
        relaunch()
        let image = XCUIScreen.main.screenshot().image.cgImage
        let size = CGSize(width: image?.width ?? 0, height: image?.height ?? 0)
        guard size == expectedSize else {
            throw ScreenshotFailure("the screenshots are \(size), not \(expectedSize)")
        }
        // The zone Notification Center's clock follows, which the CI job picks.
        let zone = TimeZone.current
        let clock = XCTAttachment(
            string: "\(zone.identifier), \(zone.secondsFromGMT()) s from GMT, at \(Date())"
        )
        clock.name = "simulator clock"
        clock.lifetime = .keepAlways
        test.add(clock)
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

    // An iPad in windowed mode draws a resize grabber in the app's corner, in every shot.
    // Settings switches it to full-screen apps. Its tree is kept, in case the choice moved.
    private func useFullScreenApps() {
        let settings = XCUIApplication(bundleIdentifier: settingsBundleId)
        settings.launch()
        let section = settings.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", multitaskingSection)).firstMatch
        if section.waitForExistence(timeout: shortTimeout), section.isHittable {
            section.tap()
            let option = settings.descendants(matching: .any)
                .matching(NSPredicate(format: "label == %@", fullScreenAppsOption)).firstMatch
            if option.waitForExistence(timeout: shortTimeout), option.isHittable {
                option.tap()
                RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
            }
        }
        let tree = XCTAttachment(string: settings.debugDescription)
        tree.name = "settings tree"
        tree.lifetime = .keepAlways
        test.add(tree)
        settings.terminate()
    }

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
    func normalize() throws {
        test.closeTheDialogIfKept(in: app)
        try closeSearch()
    }

    var searchField: XCUIElement {
        app.textViews.firstMatch
    }

    /// Opens the tab the other way round first, so its screen is built anew: a fresh Favorites
    /// turns extra info off, and a fresh Main starts at the top of its list. Tapping the tab
    /// already shown keeps its screen.
    func openTabAnew(_ tab: String) throws {
        try show(tab == listTab ? favoritesTab : listTab)
        try show(tab)
    }

    // A tap that lands while the screen still settles is lost, so a tab whose screen does not
    // show is tapped once more.
    private func show(_ tab: String) throws {
        let button = test.waitFor(app.tagged(tab))
        button.tap()
        let quick = waitUntil(shortTimeout) { self.isShown(tab) }
        if !quick {
            button.tap()
        }
        let shown = waitUntil(loadTimeout) { self.isShown(tab) }
        guard shown else {
            throw ScreenshotFailure("the screen of \(tab) did not open")
        }
    }

    private func isShown(_ tab: String) -> Bool {
        let onMain = app.tagged("ongoing_button").exists
        let favorite = app.allTagged(favoriteItemTag).firstMatch.exists
        if tab == listTab {
            return onMain && !favorite
        }
        let emptyText = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", favoritesEmptyStart)).firstMatch
        return !onMain && (favorite || emptyText.exists)
    }

    private func openList(section: String, top: Anchor?, dateMode: [String]) throws {
        try normalize()
        try openTabAnew(listTab)
        let sectionTab = app.tagged(section)
        sectionTab.tap()
        let selected = waitUntil(shortTimeout) { sectionTab.isSelected }
        if !selected {
            sectionTab.tap()
        }
        // The section's own page replaces the one shown before it.
        RunLoop.current.run(until: Date().addingTimeInterval(sectionLoadTime))
        try measureListStart(itemTag: listItemTag)
        if let top {
            try align(to: top, itemTag: listItemTag)
        }
        try showDates(on: dateMode)
        screen = .list(anchor: top, dateMode: dateMode, shows: [])
    }

    private func openSearchResults(for query: String, dateMode: [String], shows: [String]) throws {
        try normalize()
        try openTabAnew(listTab)
        try search(query)
        let found = waitForResults {
            (dateMode + shows).allSatisfy { self.mainCard($0) != nil }
        }
        guard found else {
            throw ScreenshotFailure("search \"\(query)\" did not show \(dateMode + shows)")
        }
        // The shot starts at the first result, wherever the results left the list.
        scrollToListTop()
        try measureListStart(itemTag: listItemTag)
        try showDates(on: dateMode)
        screen = .list(anchor: nil, dateMode: dateMode, shows: shows)
    }

    func openFavorites(top: Anchor?, extraInfo: [String]) throws {
        try normalize()
        try openTabAnew(favoritesTab)
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

/// How Favorites' text for an empty list starts.
let favoritesEmptyStart = "You haven"

let settingsBundleId = "com.apple.Preferences"
let multitaskingSection = "Multitasking & Gestures"
let fullScreenAppsOption = "Full Screen Apps"

let listTab = "anime_list_button"
let favoritesTab = "anime_favorites_button"
let listItemTag = "anime_list_item"
let favoriteItemTag = "anime_favorites_item"

/// How long a short wait lasts: a field closing, a keyboard going, a window turning.
let shortTimeout: TimeInterval = 10

/// How long the screen takes to settle after a tap or a drag.
let settleTime: TimeInterval = 0.6

/// How long a Main section takes to load its first page after its tab is tapped.
let sectionLoadTime: TimeInterval = 3
