import XCTest

/// Notification Center over the app: opening it, laying out the app's notifications for a
/// shot, and closing it.
extension ScreenshotDriver {
    /// Pulls Notification Center down over the app and waits until it lists a named title.
    func openNotificationCenter(listing titles: [String]) throws {
        // An iPad's top edge opens the app's menu bar except over the clock.
        let pullXs = isPad ? padPullXs : phonePullXs
        for pullX in pullXs {
            swipe(
                springboard,
                from: CGVector(dx: pullX, dy: topEdgeY),
                to: CGVector(dx: pullX, dy: pullDownEndY)
            )
            if waitUntil(revealWait, { self.notificationCenterIsOpen }) {
                break
            }
        }
        guard notificationCenterIsOpen else {
            attachSpringboardTree()
            throw ScreenshotFailure("Notification Center did not open")
        }
        let shown = waitUntil(revealWait) { titles.contains { self.notification($0).exists } }
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

    func notification(_ title: String) -> XCUIElement {
        springboard.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", title)).firstMatch
    }

    // Notification Center opens with every notification in one stack, and a tap spreads it. The
    // app's group stays collapsed to its newest notification until a tap on it. A tap on an
    // expanded notification would open the app instead, so each tap needs the group collapsed.
    func expandNotifications(_ titles: [String], collapsedName: String) throws {
        guard let newest = titles.first else { return }
        if !allListed(titles) {
            capture(collapsedName)
        }
        for _ in 0..<expandTaps {
            clearOtherNotifications(beside: newest)
            if waitUntil(expandSettle, { self.allListed(titles) }) {
                break
            }
            guard groupCollapsed, notification(newest).isHittable else {
                break
            }
            notification(newest).tap()
            RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
            guard notification(newest).exists else {
                throw ScreenshotFailure("the tap left Notification Center instead of expanding it")
            }
        }
        clearOtherNotifications(beside: newest)
        let listed = waitUntil(shortTimeout) { self.allListed(titles) }
        guard listed else {
            attachSpringboardTree()
            throw ScreenshotFailure("Notification Center does not list all of \(titles)")
        }
    }

    // Another app's notification, such as the system's welcome, would share the shot. A swipe to
    // the left uncovers its Clear button. One still stacked under the app's group is left alone,
    // since a swipe there would clear the group.
    private func clearOtherNotifications(beside newest: String) {
        for _ in 0..<maxClears {
            let other = springboard.descendants(matching: .any)
                .matching(identifier: notificationViewId)
                .matching(NSPredicate(format: "NOT (label BEGINSWITH %@)", appNotificationLabel))
                .firstMatch
            let ours = notification(newest)
            guard other.exists, other.isHittable,
                  !ours.exists || !other.frame.intersects(ours.frame) else {
                return
            }
            other.swipeLeft()
            let clear = springboard.buttons[clearLabel]
            guard clear.waitForExistence(timeout: shortTimeout) else {
                attachSpringboardTree()
                return
            }
            clear.tap()
            RunLoop.current.run(until: Date().addingTimeInterval(expandSettle))
        }
    }

    private var groupCollapsed: Bool {
        springboard.descendants(matching: .any)
            .matching(NSPredicate(format: "label BEGINSWITH %@", collapsedGroupLabel))
            .firstMatch.exists
    }

    // The large clock of Notification Center lies within the screen only while it is open.
    private var notificationCenterIsOpen: Bool {
        let clock = springboard.otherElements[notificationCenterClockId]
        return clock.exists && clock.frame.minY >= 0
    }

    // Every title shows, each in a notification of its own.
    private func allListed(_ titles: [String]) -> Bool {
        let frames = titles.map { notification($0) }
            .filter { $0.isHittable }
            .map { $0.frame }
        let rows = Set(frames.map { Int($0.minY) })
        return frames.count == titles.count && rows.count == titles.count
    }

    // A long press opens the newest notification large, with its poster, which can take seconds
    // to open. Its captures are extras, whatever the press showed.
    func pressNewestNotification(_ titles: [String], name: String) {
        guard let newest = titles.first else { return }
        let element = notification(newest)
        guard element.isHittable else { return }
        element.press(forDuration: longPressDuration)
        for number in pressedCaptureNumbers {
            RunLoop.current.run(until: Date().addingTimeInterval(captureSpacing))
            capture(name + number)
        }
    }

    // The app counts as in front even under Notification Center, so the list going away is what
    // proves it closed. A home press may close only an open preview.
    func closeNotificationCenter(listing titles: [String]) throws {
        let newest = notification(titles.first ?? "")
        for _ in 0..<homePresses {
            XCUIDevice.shared.press(.home)
            RunLoop.current.run(until: Date().addingTimeInterval(settleTime))
            if test.waitUntilGone(newest, timeout: shortTimeout) {
                break
            }
        }
        app.activate()
        let back = waitUntil(shortTimeout) { !newest.exists && self.app.tagged(listTab).isHittable }
        guard back else {
            throw ScreenshotFailure("the app did not come back after Notification Center")
        }
    }

    private func swipe(_ target: XCUIApplication, from start: CGVector, to end: CGVector) {
        let destination = target.coordinate(withNormalizedOffset: end)
        target.coordinate(withNormalizedOffset: start)
            .press(forDuration: pressDuration, thenDragTo: destination)
    }

    /// Keeps the home screen's element tree and the screen, to show what a failed step met.
    func attachSpringboardTree() {
        let attachment = XCTAttachment(string: springboard.debugDescription)
        attachment.name = "springboard tree"
        attachment.lifetime = .keepAlways
        test.add(attachment)
        test.keepScreenshot("springboard screen")
    }
}

/// How long a press opens a notification large.
let longPressDuration: TimeInterval = 2.5

/// Added to the name of each capture of a pressed notification. The collector reads a name with
/// one tilde only, so these are numbers.
let pressedCaptureNumbers = ["", "2", "3"]

/// How long Notification Center may take to list notifications once pulled down.
let revealWait: TimeInterval = 3

/// How many home presses closing Notification Center may take.
let homePresses = 2

/// Where across the screen Notification Center is pulled down, tried in turn: left of centre,
/// since the right side opens Control Center. An iPad's are over its clock and its date. These
/// and the points below are normalized to the screen.
let phonePullX: CGFloat = 0.3
let phoneClockX: CGFloat = 0.15
let padClockX: CGFloat = 0.05
let padDateX: CGFloat = 0.12
let phonePullXs = [phonePullX, phoneClockX]
let padPullXs = [padClockX, padDateX]

/// The pull starts at the top edge and ends past the middle.
let topEdgeY: CGFloat = 0.01
let pullDownEndY: CGFloat = 0.6

/// The drag up that brings the notification list from below the clock.
let revealX: CGFloat = 0.5
let revealStartY: CGFloat = 0.75
let revealEndY: CGFloat = 0.35

let listRevealStart = CGVector(dx: revealX, dy: revealStartY)
let listRevealEnd = CGVector(dx: revealX, dy: revealEndY)

/// How many taps expanding the app's group may take: one spreads the stack, one opens the group.
let expandTaps = 4

/// How long Notification Center takes to lay out its list after a tap.
let expandSettle: TimeInterval = 2

/// How many other apps' notifications are cleared before a shot.
let maxClears = 3

/// The identifier of Notification Center's large clock.
let notificationCenterClockId = "lockscreen-date-view"

/// The identifier every notification's view carries.
let notificationViewId = "NotificationShortLookView"

/// How the labels of the app's notifications start, and of its collapsed group.
let appNotificationLabel = "ANOTI, "
let collapsedGroupLabel = "ANOTI, Grouped"

/// The button a swipe to the left uncovers on a notification.
let clearLabel = "Clear"
