import KMP
import SwiftUI

struct ThirdComponent: View {
    private let screen: ThirdScreen

    init(_ screen: ThirdScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { ThirdScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.third_screen_title.localized)
    }
}
