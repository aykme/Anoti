import SwiftUI

/// The iOS app: one window showing the shared screen.
@main
struct AnotiApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
