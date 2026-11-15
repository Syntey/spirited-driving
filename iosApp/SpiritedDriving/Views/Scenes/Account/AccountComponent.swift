import KMP
import SwiftUI

struct AccountComponent: View {
    private let screen: AccountScreen

    init(_ screen: AccountScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { AccountScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.account_screen_title.localized)
            .navigationBarTitleDisplayMode(.inline)
    }
}
