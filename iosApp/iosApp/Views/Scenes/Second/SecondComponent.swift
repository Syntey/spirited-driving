import KMP
import SwiftUI

struct SecondComponent: View {
    private let screen: SecondScreen

    init(_ screen: SecondScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { SecondScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.second_screen_title.localized)
    }
}
