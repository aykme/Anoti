import UIKit

/// One step of a screenshot scenario. A scenario is a list of them, read top to bottom; screen
/// steps set up what the next `shoot` keeps. Steps are data, so a new set of screenshots is a new
/// list, not new code. Titles are exact Shikimori names.
enum ScreenshotStep: Sendable {
    /// Searches each title on Main and turns its bell on.
    case follow([String])

    /// One update pass that notifies exactly these titles. A pass handles favorites in their
    /// list order whatever the order here; Notification Center shows the latest pass on top.
    case notify([String])

    /// Waits, so Notification Center shows the passes at different ages.
    case pause(TimeInterval)

    /// Edits the favorites in the app's database between two launches.
    case database(DatabaseEdit)

    /// Turns the device: `.portrait` or `.landscapeLeft`. Only an iPad's window follows.
    case turn(UIDeviceOrientation)

    /// On air on Main, scrolled to `top`, with the date shown on the `dateMode` cards.
    case onAir(top: Anchor? = nil, dateMode: [String] = [])

    /// Soon on Main, scrolled to `top`, with the start date shown on the `dateMode` cards.
    case soon(top: Anchor? = nil, dateMode: [String] = [])

    /// Main's search for a query, with the date shown on the `dateMode` cards. The `shows` cards
    /// must be on screen.
    case search(String, dateMode: [String] = [], shows: [String] = [])

    /// Favorites opened anew, scrolled to `top`, with extra info on the `extraInfo` cards.
    case favorites(top: Anchor? = nil, extraInfo: [String] = [])

    /// Notification Center over the app, its Anoti group expanded to list these titles.
    case notificationCenter([String])

    /// Checks the screen the last step set up, then keeps it under this name.
    case shoot(String)
}

/// The card a list is scrolled to: it sits at the list's top, raised by `raisedBy` points.
struct Anchor: Sendable {
    /// The card's title.
    let title: String

    /// How far the card sits above the list's top, partly under the top bar.
    var raisedBy: CGFloat = 0
}

/// A change to the favorites made in the database, where the app has no control for it.
struct DatabaseEdit: Sendable {
    /// Favorites removed, as the bell on their card would.
    var unfollow: [String] = []

    /// The favorites that show "New episode"; every other one does not. Nil leaves them as
    /// they are.
    var newEpisodes: [String]?

    /// The episodes-viewed count set on each favorite named.
    var episodesViewed: [String: Int] = [:]
}
