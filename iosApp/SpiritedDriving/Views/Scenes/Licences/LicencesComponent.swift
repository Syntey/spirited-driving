import KMP
import SwiftUI

struct LicencesComponent: View {
    private let screen: LicencesScreen

    init(_ screen: LicencesScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { LicencesScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.licences_screen_title.localized)
            .navigationBarTitleDisplayMode(.inline)
    }
}
