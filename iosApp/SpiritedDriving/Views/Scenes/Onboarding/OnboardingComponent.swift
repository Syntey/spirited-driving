import KMP
import SwiftUI

struct OnboardingComponent: View {
    private let screen: OnboardingScreen

    init(_ screen: OnboardingScreen) {
        self.screen = screen
    }

    var body: some View {
        // The illustration fills the screen; Compose keeps Skip and the panel clear of the status bar and home indicator.
        ComposeView { OnboardingScreenUiController(screen: screen) }
            .composeScreen()
    }
}
