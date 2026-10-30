import XCTest

/// Opening the newest notification of Notification Center large, with its poster.
extension ScreenshotDriver {
    // A long press opens the newest notification large, with its poster. iOS takes the test's
    // press only now and then, and the opened notification is missing from the element tree. So
    // each way below is tried until the screen changes as much as an opened one changes it. A
    // note keeps what each way did. The captures are extras, whatever the ways showed.
    func openNewestNotification(_ titles: [String], name: String) {
        guard let newest = titles.first else { return }
        var notes = ["pressure supported: \(supportsPressure)"]
        var place = OpeningPlace.overApp
        var opened = false
        for way in openingWays where !opened {
            if way.place != place {
                do {
                    try move(to: way.place, listing: titles, name: name)
                    place = way.place
                } catch {
                    notes.append("\(way.place): \(error)")
                    break
                }
            }
            let cell = notification(newest)
            guard cell.isHittable else {
                notes.append("\(way): the notification is not hittable")
                continue
            }
            let before = screenThumbnail()
            guard press(cell, by: way.touch) else {
                notes.append("\(way): skipped")
                continue
            }
            var change = 0.0
            opened = waitUntil(openTimeout) {
                change = Self.change(from: before, to: self.screenThumbnail())
                return change > openedChange
            }
            notes.append("\(way): change \(change), opened \(opened)")
        }
        let note = XCTAttachment(string: notes.joined(separator: "\n"))
        note.name = "long press"
        note.lifetime = .keepAlways
        test.add(note)
        if opened {
            // The poster arrives after the notification opens.
            RunLoop.current.run(until: Date().addingTimeInterval(posterLoadTime))
        }
        for number in pressedCaptureNumbers {
            RunLoop.current.run(until: Date().addingTimeInterval(captureSpacing))
            capture(name + pressedSuffix + number)
        }
    }

    // Opens Notification Center anew: over the home screen, or once the app has ended.
    private func move(to place: OpeningPlace, listing titles: [String], name: String) throws {
        dismissNotificationCenter(notification(titles.first ?? ""))
        switch place {
        case .overApp:
            break
        case .overHome:
            XCUIDevice.shared.press(.home)
        case .afterEnd:
            app.terminate()
            _ = waitUntil(shortTimeout) { self.app.state == .notRunning }
        }
        RunLoop.current.run(until: Date().addingTimeInterval(placeSettle))
        try openNotificationCenter(listing: titles, opensNewest: true)
        try expandNotifications(titles, collapsedName: name + "~" + place.rawValue)
    }

    // Tells whether the touch was made.
    private func press(_ cell: XCUIElement, by touch: OpeningTouch) -> Bool {
        let point = cell.coordinate(withNormalizedOffset: CGVector(dx: middleAcross, dy: pressDown))
        switch touch {
        case .hold:
            point.press(forDuration: longHold)
        case .hid:
            let frame = cell.frame
            return hidPress(at: CGPoint(x: frame.midX, y: frame.midY), for: longHold)
        case .force:
            return forcePress(point)
        case .holdAndDrag:
            point.press(
                forDuration: shortHold,
                thenDragTo: point.withOffset(CGVector(dx: 0, dy: dragDown)),
                withVelocity: .slow,
                thenHoldForDuration: longHold
            )
        }
        return true
    }

    // AXe in the CI job presses through the simulator's own touch input, as a mouse in Simulator
    // does, not through XCTest. The job takes the request from a file and answers with another.
    private func hidPress(at point: CGPoint, for duration: TimeInterval) -> Bool {
        guard let folder = ProcessInfo.processInfo.environment[hidPressFolderKey] else {
            return false
        }
        let request = URL(fileURLWithPath: folder).appendingPathComponent("request")
        let done = URL(fileURLWithPath: folder).appendingPathComponent("done")
        try? FileManager.default.removeItem(at: done)
        let line = "\(Int(point.x.rounded())) \(Int(point.y.rounded())) \(duration)\n"
        guard (try? line.write(to: request, atomically: true, encoding: .utf8)) != nil else {
            return false
        }
        return waitUntil(hidPressTimeout) { FileManager.default.fileExists(atPath: done.path) }
    }

    // XCTest's hidden force press. A device without pressure support would fail the test on it.
    private func forcePress(_ point: XCUICoordinate) -> Bool {
        let selector = NSSelectorFromString("forcePress")
        guard supportsPressure, point.responds(to: selector),
              let implementation = point.method(for: selector) else {
            return false
        }
        typealias Action = @convention(c) (AnyObject, Selector) -> Void
        unsafeBitCast(implementation, to: Action.self)(point, selector)
        return true
    }

    private var supportsPressure: Bool {
        let selector = NSSelectorFromString("supportsPressureInteraction")
        let device = XCUIDevice.shared
        guard device.responds(to: selector),
              let implementation = device.method(for: selector) else {
            return false
        }
        typealias Getter = @convention(c) (AnyObject, Selector) -> Bool
        return unsafeBitCast(implementation, to: Getter.self)(device, selector)
    }

    // The screen drawn into a few grey pixels: enough to tell a large change from a small one.
    private func screenThumbnail() -> [UInt8] {
        guard let image = XCUIScreen.main.screenshot().image.cgImage else { return [] }
        var pixels = [UInt8](repeating: 0, count: thumbnailWidth * thumbnailHeight)
        pixels.withUnsafeMutableBytes { buffer in
            let context = CGContext(
                data: buffer.baseAddress,
                width: thumbnailWidth,
                height: thumbnailHeight,
                bitsPerComponent: thumbnailBitsPerPixel,
                bytesPerRow: thumbnailWidth,
                space: CGColorSpaceCreateDeviceGray(),
                bitmapInfo: CGImageAlphaInfo.none.rawValue
            )
            let area = CGRect(x: 0, y: 0, width: thumbnailWidth, height: thumbnailHeight)
            context?.draw(image, in: area)
        }
        return pixels
    }

    // The mean difference of two thumbnails, from 0 for the same to 1 for black against white.
    private static func change(from before: [UInt8], to after: [UInt8]) -> Double {
        guard !before.isEmpty, before.count == after.count else { return 0 }
        let total = zip(before, after).reduce(0) { sum, pair in
            sum + abs(Int(pair.0) - Int(pair.1))
        }
        return Double(total) / Double(before.count * Int(UInt8.max))
    }
}

/// Where Notification Center is open while a way presses the newest notification. Each name
/// also names the capture of the group as it opens collapsed there.
private enum OpeningPlace: String {
    case overApp = "app"
    case overHome = "home"
    case afterEnd = "ended"
}

/// How a way touches the newest notification: XCTest's long press, AXe's long press, XCTest's
/// force press, or a long press that drags down.
private enum OpeningTouch {
    case hold, hid, force, holdAndDrag
}

private struct OpeningWay: CustomStringConvertible {
    let place: OpeningPlace
    let touch: OpeningTouch

    var description: String { "\(place) \(touch)" }
}

/// The ways tried in turn, until one opens the newest notification. An iPad's opens by the first.
private let openingWays = [
    OpeningWay(place: .overApp, touch: .hold), OpeningWay(place: .overApp, touch: .hid),
    OpeningWay(place: .overApp, touch: .force), OpeningWay(place: .overApp, touch: .holdAndDrag),
    OpeningWay(place: .overHome, touch: .hold), OpeningWay(place: .overHome, touch: .hid),
    OpeningWay(place: .afterEnd, touch: .hold), OpeningWay(place: .afterEnd, touch: .hid)
]

let longHold: TimeInterval = 2.5
let shortHold: TimeInterval = 1

/// How far down the dragging press moves, in points.
let dragDown: CGFloat = 60

/// Where across and down the notification a press lands: its middle.
let middleAcross: CGFloat = 0.5
let pressDown: CGFloat = 0.5

/// How long the app takes to leave the screen, or to end, before Notification Center opens anew.
let placeSettle: TimeInterval = 3

/// The environment key of the folder through which the CI job's AXe takes a press. Without it
/// no such press is made.
let hidPressFolderKey = "ANOTI_PRESS_DIR"

/// How long AXe may take to make a press.
let hidPressTimeout: TimeInterval = 30

/// How long a press may take to open a notification large.
let openTimeout: TimeInterval = 6

/// How much of the screen an opened notification changes: it blurs the list and shows a poster.
/// A highlighted row or a ticking time changes far less.
let openedChange = 0.05

/// How long the poster of an opened notification takes to show.
let posterLoadTime: TimeInterval = 2

/// The size of the grey thumbnail a screen is compared by, one byte a pixel.
let thumbnailWidth = 32
let thumbnailHeight = 64
let thumbnailBitsPerPixel = 8

/// Added to the name of each capture of a pressed notification. The collector reads a name with
/// one tilde only, so these are numbers.
let pressedSuffix = "~pressed"
let pressedCaptureNumbers = ["", "2", "3"]
