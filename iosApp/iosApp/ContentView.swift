import Shared
import SwiftUI
import UIKit

/// The app's one screen: the shared Compose UI, with its state kept in the scene's storage.
struct ContentView: View {
    @SceneStorage("rootState") private var rootState = ""
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        ComposeView(restoredState: rootState)
            .ignoresSafeArea()
            .background(
                GeometryReader { proxy in
                    // Black, so nothing white shows before Compose draws.
                    Color.black.onChange(of: proxy.size) { _ in
                        refreshSupportedOrientations()
                    }
                }
                .ignoresSafeArea()
            )
            .onChange(of: scenePhase) { phase in
                guard phase != .active, let saved = IosApp.shared.saveState() else { return }
                rootState = saved
            }
    }
}

/// Hosts the shared screen. Built once per scene; SwiftUI keeps its controller.
private struct ComposeView: UIViewControllerRepresentable {
    let restoredState: String

    func makeUIViewController(context: Context) -> UIViewController {
        IosApp.shared.viewController(restoredState: restoredState)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// A window that grows past or shrinks below the rotation threshold changes how it may turn.
private func refreshSupportedOrientations() {
    UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .forEach { scene in
            scene.keyWindow?.rootViewController?.setNeedsUpdateOfSupportedInterfaceOrientations()
        }
}
