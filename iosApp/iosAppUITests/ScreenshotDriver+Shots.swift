import XCTest

/// Composing the last details of a screen, checking it, and keeping it, Notification Center
/// included.
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
    /// moves the ones below it, never the ones above.
    func turnOnExtraInfo(for titles: [String]) throws {
        let frames = try titles.map { title in
            guard let card = favoriteCard(title) else {
                throw ScreenshotFailure("\(title) is not among the favorites on screen")
            }
            return card.frame
        }
        let lowestFirst = frames.sorted { $0.minY > $1.minY }
        for frame in lowestFirst {
            let before = extraInfoCards.count
            guard let button = element(labelled: extraInfoOnLabel, inside: frame) else {
                throw ScreenshotFailure("a favorite at \(frame.minY) has no extra info to turn on")
            }
            button.tap()
            let turned = waitUntil(shortTimeout) { self.extraInfoCards.count == before + 1 }
            guard turned else {
                throw ScreenshotFailure("a favorite at \(frame.minY) did not show extra info")
            }
        }
    }

    /// Pulls Notification Center down over the app and waits until it lists a named title.
    func openNotificationCenter(listing titles: [String]) throws {
        swipe(springboard, from: notificationCenterStart, to: notificationCenterEnd)
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
        let shown = titles.contains { notification($0).exists }
        if !shown {
            // Notifications that came while the app was open can wait below the clock.
            swipe(springboard, from: listRevealStart, to: listRevealEnd)
        }
        let found = waitUntil(shortTimeout) { titles.contains { self.notification($0).exists } }
        guard found else {
            attachSpringboardTree()
            throw ScreenshotFailure("Notification Center shows none of \(titles)")
        }
        screen = .notificationCenter(titles)
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
            try closeNotificationCenter()
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
            let text = app.descendants(matching: .any)
                .matching(NSPredicate(format: "label == %@", title)).firstMatch
            guard text.exists, text.frame.maxY <= bottom else {
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
        let database = try database()
        let names = Set(try database.names())
        guard names == expectedFavorites else {
            throw ScreenshotFailure("the favorites are \(names), not \(expectedFavorites)")
        }
        let extra = Set(try database.extraInfoNames())
        if extra != Set(extraInfo) {
            return "extra info is on for \(extra), not \(Set(extraInfo))"
        }
        let flagged = Set(try database.newEpisodeNames())
        if flagged != expectedNewEpisodes {
            return "new episodes show on \(flagged), not \(expectedNewEpisodes)"
        }
        let tab = app.tagged(favoritesTab)
        let label = favoritesLabel(newEpisodes: flagged.count)
        if !tab.shows(label) || (flagged.isEmpty && tab.label.contains(withNewEpisodes)) {
            return "the Favorites tab reads \"\(tab.label)\", not \"\(label)\""
        }
        let shownExtra = extraInfoCards.filter { $0.frame.maxY <= tabBarTop }.count
        if shownExtra != extraInfo.count {
            return "\(shownExtra) favorites show extra info on screen, not \(extraInfo.count)"
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
        if isWide(app.windows.firstMatch.frame), abs(frame.minX - listLeft) > columnTolerance {
            throw ScreenshotFailure("\(anchor.title) is not in the first column")
        }
    }

    // The favorite at the top of the scrolled list. One scrolled off above sits higher.
    private func topFavorite(below target: CGFloat) -> XCUIElement? {
        app.allTagged(favoriteItemTag).allElementsBoundByIndex
            .filter { $0.frame.minY >= target - 1 }
            .min { $0.frame.minY < $1.frame.minY }
    }

    var extraInfoCards: [XCUIElement] {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", extraInfoOffLabel))
            .allElementsBoundByIndex
    }

    // MARK: - Captures

    // The test keeps the screen as the device holds it; the CI job turns a landscape one upright.
    private func capture(_ name: String) {
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

    // MARK: - Notification Center

    func notification(_ title: String) -> XCUIElement {
        springboard.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", title)).firstMatch
    }

    // A collapsed group hides all but its newest notification, and a tap expands it. A tap on an
    // expanded notification would open the app instead, so the tap comes only when needed.
    private func expandNotifications(_ titles: [String], collapsedName: String) throws {
        guard !allListed(titles) else { return }
        capture(collapsedName)
        guard let newest = titles.first, notification(newest).isHittable else {
            attachSpringboardTree()
            throw ScreenshotFailure("the newest notification cannot be tapped")
        }
        notification(newest).tap()
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
        guard notification(newest).exists else {
            throw ScreenshotFailure("the tap left Notification Center instead of expanding it")
        }
        let listed = waitUntil(shortTimeout) { self.allListed(titles) }
        guard listed else {
            attachSpringboardTree()
            throw ScreenshotFailure("Notification Center does not list all of \(titles)")
        }
    }

    // Every title shows, each in a notification of its own.
    private func allListed(_ titles: [String]) -> Bool {
        let frames = titles.map { notification($0) }
            .filter { $0.isHittable }
            .map { $0.frame }
        let rows = Set(frames.map { Int($0.minY) })
        return frames.count == titles.count && rows.count == titles.count
    }

    // On a wide screen a long press opens the newest notification large, with its poster; kept
    // as an extra if it opens.
    private func pressNewestNotification(_ titles: [String], name: String) {
        guard isWide(app.windows.firstMatch.frame), let newest = titles.first else { return }
        let element = notification(newest)
        guard element.isHittable else { return }
        element.press(forDuration: longPressDuration)
        RunLoop.current.run(until: Date().addingTimeInterval(captureSpacing))
        capture(name)
    }

    private func closeNotificationCenter() throws {
        XCUIDevice.shared.press(.home)
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
        app.activate()
        guard app.wait(for: .runningForeground, timeout: shortTimeout) else {
            throw ScreenshotFailure("the app did not come back after Notification Center")
        }
    }

    private func swipe(_ target: XCUIApplication, from start: CGVector, to end: CGVector) {
        let destination = target.coordinate(withNormalizedOffset: end)
        target.coordinate(withNormalizedOffset: start)
            .press(forDuration: pressDuration, thenDragTo: destination)
    }

    func attachSpringboardTree() {
        let attachment = XCTAttachment(string: springboard.debugDescription)
        attachment.name = "springboard tree"
        attachment.lifetime = .keepAlways
        test.add(attachment)
    }
}

let showDateLabel = "Display extra episodes info"
let showEpisodesLabel = "Display information about the available episodes"
let extraInfoOnLabel = "Turn on the display of extra information"
let extraInfoOffLabel = "Turn off the display of extra information"

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

/// How long a press opens a notification large.
let longPressDuration: TimeInterval = 1.5

/// Where across the screen Notification Center is pulled down: left of centre, since the right
/// side opens Control Center. Normalized, as the next five.
let notificationCenterX: CGFloat = 0.3

/// The pull starts at the top edge and ends past the middle.
let topEdgeY: CGFloat = 0.01
let pullDownEndY: CGFloat = 0.6

/// The drag up that brings the notification list from below the clock.
let revealX: CGFloat = 0.5
let revealStartY: CGFloat = 0.75
let revealEndY: CGFloat = 0.35

let notificationCenterStart = CGVector(dx: notificationCenterX, dy: topEdgeY)
let notificationCenterEnd = CGVector(dx: notificationCenterX, dy: pullDownEndY)
let listRevealStart = CGVector(dx: revealX, dy: revealStartY)
let listRevealEnd = CGVector(dx: revealX, dy: revealEndY)
