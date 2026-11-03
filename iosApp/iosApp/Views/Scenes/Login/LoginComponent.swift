import KMP
import SwiftUI

struct LoginComponent: View {
    private let screen: LoginScreen

    init(_ screen: LoginScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { LoginScreenUiController(screen: screen) }
            .composeScreen()
    }
}
