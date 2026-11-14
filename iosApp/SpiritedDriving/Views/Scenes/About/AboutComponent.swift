import KMP
import SwiftUI

struct AboutComponent: View {
    private let screen: AboutScreen

    init(_ screen: AboutScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { AboutScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.about_screen_title.localized)
            .navigationBarTitleDisplayMode(.inline)
    }
}
