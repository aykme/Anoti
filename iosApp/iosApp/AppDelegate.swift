import Shared
import UIKit

/// Starts the shared app before launch ends and answers which way its windows may turn.
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        IosApp.shared.start()
        return true
    }

    func application(
        _ application: UIApplication,
        supportedInterfaceOrientationsFor window: UIWindow?
    ) -> UIInterfaceOrientationMask {
        // Kotlin hands the mask over as a UInt64.
        let mask = IosApp.shared.supportedInterfaceOrientations(window: window)
        return UIInterfaceOrientationMask(rawValue: UInt(mask))
    }
}
