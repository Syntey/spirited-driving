import KMP
import SwiftUI

struct FirstComponent: View {
    private let screen: FirstScreen

    init(_ screen: FirstScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { FirstScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.first_screen_title.localized)
    }
}
