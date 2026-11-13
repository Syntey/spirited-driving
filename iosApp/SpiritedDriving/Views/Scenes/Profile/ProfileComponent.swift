import KMP
import SwiftUI

struct ProfileComponent: View {
    private let screen: ProfileScreen

    init(_ screen: ProfileScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { ProfileScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.profile_screen_title.localized)
            .navigationBarTitleDisplayMode(.inline)
    }
}
