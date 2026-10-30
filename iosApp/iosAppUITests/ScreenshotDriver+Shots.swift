import XCTest

/// Composing the last details of a screen, checking it, and keeping it.
extension ScreenshotDriver {
    /// Shows the date on each Main card named, instead of its episode count.
    func showDates(on titles: [String]) throws {
        for title in titles {
            guard let card = mainCard(title) else {
                throw ScreenshotFailure("\(title) is not on screen")
            }
            let frame = card.frame
            element(labelled: showDateLabel, inside: frame)?.tap()
            let shown = waitUntil(shortTimeout) {
                self.element(labelled: showEpisodesLabel, inside: frame) != nil
            }
            guard shown else {
                throw ScreenshotFailure("\(title) did not show its date")
            }
        }
    }

    /// Turns on extra info on each favorite named, the lowest card first: a turned card grows and
    /// moves the ones below it, never the ones above. Each card must lie above the tab bar.
    ///
    /// The database tells each turn. On iOS a card's accessibility can keep its old label and text
    /// after the card changes, while the screen and the database show the change.
    func turnOnExtraInfo(for titles: [String]) throws {
        let bottom = tabBarTop
        let frames = try titles.map { title in
            guard let card = favoriteCard(title), card.frame.maxY <= bottom else {
                throw ScreenshotFailure("\(title) is not wholly among the favorites on screen")
            }
            return card.frame
        }
        let lowestFirst = frames.sorted { $0.minY > $1.minY }
        for frame in lowestFirst {
            let before = try database().extraInfoNames().count
            guard let button = element(labelled: extraInfoOnLabel, inside: frame) else {
                throw ScreenshotFailure("a favorite at \(frame.minY) has no extra info to turn on")
            }
            // A tap that lands while the screen still settles is lost; the database tells.
            var turned = false
            for _ in 0..<extraInfoTaps where !turned {
                button.tap()
                turned = waitUntil(shortTimeout) {
                    (try? self.database().extraInfoNames().count) == before + 1
                }
            }
            guard turned else {
                throw ScreenshotFailure("a favorite at \(frame.minY) did not show extra info")
            }
        }
    }

    /// Checks the screen the last step set up, then keeps it three times, a little apart, since
    /// posters keep arriving. Notification Center is expanded first and closed after.
    func shoot(_ name: String) throws {
        switch screen {
        case let .list(anchor, dateMode, shows):
            try checkList(anchor: anchor, dateMode: dateMode, shows: shows)
        case let .favorites(anchor, extraInfo):
            try checkFavorites(anchor: anchor, extraInfo: extraInfo)
        case let .notificationCenter(titles):
            try expandNotifications(titles, collapsedName: name + "~collapsed")
        case .none:
            break
        }
        for suffix in captureSuffixes {
            RunLoop.current.run(until: Date().addingTimeInterval(captureSpacing))
            capture(name + suffix)
        }
        keptShots.append(name)
        if case let .notificationCenter(titles) = screen {
            pressNewestNotification(titles, name: name + "~pressed")
            try closeNotificationCenter(listing: titles)
        }
        screen = .none
    }

    // MARK: - Checks

    private func checkList(anchor: Anchor?, dateMode: [String], shows: [String]) throws {
        let bottom = tabBarTop
        if let anchor {
            guard let card = mainCard(anchor.title) else {
                throw ScreenshotFailure("\(anchor.title) left the screen")
            }
            try checkAnchor(anchor, at: card.frame)
        }
        for title in dateMode + shows {
            guard let text = mainTitle(title)?.text, text.frame.maxY <= bottom else {
                throw ScreenshotFailure("the title of \(title) is not on screen")
            }
        }
        for title in dateMode {
            let date = mainCard(title).flatMap { card in
                element(labelled: showEpisodesLabel, inside: card.frame)
            }
            guard let date, date.frame.maxY <= bottom else {
                throw ScreenshotFailure("the date of \(title) is not on screen")
            }
        }
    }

    // A Favorites screen can lose a flag or a mode to a stray tap. It is rebuilt from the last
    // edit, twice at most. A lost favorite cannot be rebuilt.
    private func checkFavorites(anchor: Anchor?, extraInfo: [String]) throws {
        for attempt in 0...maxRepairs {
            guard let problem = try favoritesProblem(anchor: anchor, extraInfo: extraInfo) else {
                return
            }
            guard attempt < maxRepairs, let lastEdit else {
                throw ScreenshotFailure(problem)
            }
            try editDatabase { try $0.apply(lastEdit) }
            try openFavorites(top: anchor, extraInfo: extraInfo)
        }
    }

    private func favoritesProblem(anchor: Anchor?, extraInfo: [String]) throws -> String? {
        let store = try database()
        let names = Set(try store.names())
        guard names == expectedFavorites else {
            throw ScreenshotFailure("the favorites are \(names), not \(expectedFavorites)")
        }
        let extra = Set(try store.extraInfoNames())
        if extra != Set(extraInfo) {
            return "extra info is on for \(extra), not \(Set(extraInfo))"
        }
        let flagged = Set(try store.newEpisodeNames())
        if flagged != expectedNewEpisodes {
            return "new episodes show on \(flagged), not \(expectedNewEpisodes)"
        }
        if let card = misflaggedCard(skipping: Set(extraInfo)) {
            return "the card of \(card) shows the wrong \"\(newEpisodeText)\""
        }
        let tab = app.tagged(favoritesTab)
        let label = favoritesLabel(newEpisodes: flagged.count)
        if !tab.shows(label) || (flagged.isEmpty && tab.label.contains(withNewEpisodes)) {
            return "the Favorites tab reads \"\(tab.label)\", not \"\(label)\""
        }
        if let anchor, let top = topFavorite(below: listTop - anchor.raisedBy) {
            try checkAnchor(anchor, at: top.frame)
        }
        return nil
    }

    private func checkAnchor(_ anchor: Anchor, at frame: CGRect) throws {
        let error = frame.minY - (listTop - anchor.raisedBy)
        guard error >= -1, error <= alignTolerance + 1 else {
            throw ScreenshotFailure("\(anchor.title) sits \(error) points off the list's top")
        }
        if isPad, abs(frame.minX - listLeft) > columnTolerance {
            throw ScreenshotFailure("\(anchor.title) is not in the first column")
        }
    }

    // A visible favorite in its normal mode shows "New episode" exactly when it is flagged. One in
    // extra info shows no title to tell it by.
    private func misflaggedCard(skipping extraInfo: Set<String>) -> String? {
        expectedFavorites.subtracting(extraInfo).sorted().first { title in
            guard let card = favoriteCard(title), card.frame.maxY <= tabBarTop else {
                return false
            }
            return card.label.contains(newEpisodeText) != expectedNewEpisodes.contains(title)
        }
    }

    // The favorite at the top of the scrolled list. One scrolled off above sits higher.
    private func topFavorite(below target: CGFloat) -> XCUIElement? {
        app.allTagged(favoriteItemTag).allElementsBoundByIndex
            .filter { $0.frame.minY >= target - 1 }
            .min { $0.frame.minY < $1.frame.minY }
    }

    // MARK: - Captures

    /// Keeps the screen as the device holds it; the CI job turns a landscape one upright.
    func capture(_ name: String) {
        let screenshot = XCUIScreen.main.screenshot()
        let attachment = XCTAttachment(
            data: screenshot.pngRepresentation,
            uniformTypeIdentifier: "public.png"
        )
        let landscape = XCUIDevice.shared.orientation.isLandscape
        attachment.name = landscape ? name + landscapeMarker : name
        attachment.lifetime = .keepAlways
        test.add(attachment)
    }
}

let showDateLabel = "Display extra episodes info"
let showEpisodesLabel = "Display information about the available episodes"
let extraInfoOnLabel = "Turn on the display of extra information"

/// How many taps a favorite's extra-info toggle gets before its turn fails.
let extraInfoTaps = 2

/// Added to a landscape capture's name; the CI job turns such a shot upright and drops it.
let landscapeMarker = "@landscape"

/// The main capture of a shot and its two later candidates.
let captureSuffixes = ["", "~2", "~3"]

/// The time between captures, and before the first.
let captureSpacing: TimeInterval = 2

/// How many times a Favorites screen is rebuilt before its check fails the test.
let maxRepairs = 2

/// How far an anchor may sit from the list's left edge and still be in the first column.
let columnTolerance: CGFloat = 20
