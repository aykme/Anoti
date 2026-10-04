import SwiftUI

/// The iOS app: one window showing the shared screen.
@main
struct AnotiApp: App {
    // SwiftUI creates the delegate through this wrapper. Nothing reads the property itself.
    // swiftlint:disable:next unused_declaration
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
