import SwiftUI
import UIKit

/**
 Hosts a Compose Multiplatform screen in SwiftUI.

 Screens are implemented once in `shared:ui` and exposed to Swift as `*ScreenUiController` functions,
 while navigation stays native (`NavigationStack`, `TabView`, sheets) and is driven by Decompose.
 */
struct ComposeView: UIViewControllerRepresentable {
    private let makeViewController: () -> UIViewController

    init(_ makeViewController: @escaping () -> UIViewController) {
        self.makeViewController = makeViewController
    }

    func makeUIViewController(context: Context) -> UIViewController {
        makeViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

extension View {
    /// Lays the Compose screen out edge to edge, under native bars and the keyboard.
    ///
    /// The screen background then reaches behind the bars, and Compose keeps its content clear of them,
    /// as it receives the safe area (including the keyboard) through `WindowInsets.safeDrawing`.
    func composeScreen() -> some View {
        ignoresSafeArea()
    }
}
