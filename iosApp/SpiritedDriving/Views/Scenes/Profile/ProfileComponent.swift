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
            .navigationTitle(screen.title.localized())
            .navigationBarTitleDisplayMode(.inline)
            // The header illustration reaches behind the bar, so the bar shows only its buttons
            .toolbarBackground(.hidden, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Color.clear.frame(width: 0, height: 0)
                }
            }
    }
}
