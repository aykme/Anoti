import Foundation
import XCUIAutomation

/// Finding cards and bars on the app's screens, and scrolling its lists without a tap.
extension ScreenshotDriver {
    // MARK: - Finding things

    /// The Main card with this exact title, found by the card's frame around the title text.
    func mainCard(_ title: String) -> XCUIElement? {
        mainTitle(title)?.card
    }

    /// The title text of a Main card, and the card. The search field can hold the same text, but
    /// it lies in no card.
    func mainTitle(_ title: String) -> (text: XCUIElement, card: XCUIElement)? {
        let cards = app.allTagged(listItemTag).allElementsBoundByIndex
        let texts = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", title))
            .allElementsBoundByIndex
        for text in texts {
            let centre = CGPoint(x: text.frame.midX, y: text.frame.midY)
            let card = cards.first { $0.frame.contains(centre) }
            if let card {
                return (text, card)
            }
        }
        return nil
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

    /// The bottom of Main's top bar, drawn over the list: its tabs, or the search field in their
    /// place. Zero where there is none.
    var topBarBottom: CGFloat {
        let tabs = app.tagged("search_button")
        if tabs.exists {
            return tabs.frame.maxY
        }
        return searchField.exists ? searchField.frame.maxY : 0
    }

    // MARK: - Scrolling

    func measureListStart(itemTag: String) throws {
        let loaded = waitUntil(loadTimeout) { self.app.allTagged(itemTag).firstMatch.exists }
        guard loaded else {
            throw ScreenshotFailure("the list did not load")
        }
        RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
        let frames = app.allTagged(itemTag).allElementsBoundByIndex.map { $0.frame }
        guard let top = frames.map(\.minY).min(), let left = frames.map(\.minX).min() else {
            throw ScreenshotFailure("the list emptied while it was measured")
        }
        listTop = top
        listLeft = left
    }

    /// Scrolls until the anchor's card sits within a few points below its target. A long move is
    /// one drag; a short one is a pair, out and back, whose touch slops cancel.
    func align(to anchor: Anchor, itemTag: String) throws {
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

    /// Waits for `condition` on Main's results. Results that arrive in steps can leave the list
    /// scrolled past its first cards, so a slow condition gets the list pulled back to its top.
    func waitForResults(_ condition: () -> Bool) -> Bool {
        if waitUntil(shortTimeout, condition) {
            return true
        }
        scrollToListTop()
        return waitUntil(loadTimeout, condition)
    }

    /// Flicks Main's list back to its first card, until the cards stop moving. A flick's touch
    /// stays short of what starts a refresh when the list is already at its top, and its glide
    /// starts none.
    func scrollToListTop() {
        for _ in 0..<maxTopFlicks {
            let before = cardFrames
            drag(by: topFlickLength, itemTag: listItemTag, flick: true)
            if cardFrames == before {
                return
            }
        }
    }

    private var cardFrames: [CGRect] {
        app.allTagged(listItemTag).allElementsBoundByIndex.map(\.frame)
    }

    /// Moves the list's content by `deltaY` points with one press-and-drag. The press is short, so
    /// no long press fires, and a hold at the end keeps the list from flinging. A `flick` drags
    /// fast and lets go, so the list glides on. The drag starts away from the top bar and, on
    /// Favorites, over the posters' column.
    func drag(by deltaY: CGFloat, itemTag: String, flick: Bool = false) {
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
            withVelocity: flick ? .fast : .slow,
            thenHoldForDuration: flick ? 0 : holdDuration
        )
        RunLoop.current.run(until: Date().addingTimeInterval(flick ? glideTime : settleTime))
    }
}

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

/// The most flicks that bring Main's list back to its top: a whole page of results.
let maxTopFlicks = 15

/// How far one flick's touch pulls Main's list down. Past the top, half of it moves the refresh
/// indicator, which refreshes at 80 points.
let topFlickLength: CGFloat = 140

/// How long a flicked list takes to glide to a stop.
let glideTime: TimeInterval = 2.5
