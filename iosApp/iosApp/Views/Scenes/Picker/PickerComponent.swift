import KMP
import SwiftUI

struct PickerComponent: View {
    private let screen: PickerScreen

    init(_ screen: PickerScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { PickerScreenUiController(screen: screen) }
            .composeScreen()
    }
}
