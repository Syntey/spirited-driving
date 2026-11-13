import KMP
import SwiftUI

struct SettingsComponent: View {
    private let screen: SettingsScreen

    init(_ screen: SettingsScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { SettingsScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.settings_screen_title.localized)
            .navigationBarTitleDisplayMode(.inline)
    }
}
