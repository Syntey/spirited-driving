import KMP
import SwiftUI

struct LicenceTextComponent: View {
    private let screen: LicenceTextScreen

    init(_ screen: LicenceTextScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { LicenceTextScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(screen.title)
            .navigationBarTitleDisplayMode(.inline)
    }
}
