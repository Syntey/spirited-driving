import KMP
import SwiftUI

struct FeedbackComponent: View {
    private let screen: FeedbackScreen

    init(_ screen: FeedbackScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { FeedbackScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(screen.title.localized())
            .navigationBarTitleDisplayMode(.inline)
    }
}
