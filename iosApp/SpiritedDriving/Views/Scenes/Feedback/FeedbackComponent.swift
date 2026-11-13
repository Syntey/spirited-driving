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
            .navigationTitle(Localizable.feedback_screen_title.localized)
            .navigationBarTitleDisplayMode(.inline)
    }
}
