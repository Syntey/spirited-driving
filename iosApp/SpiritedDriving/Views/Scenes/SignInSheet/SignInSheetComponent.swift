import KMP
import SwiftUI

struct SignInSheetComponent: View {
    private let screen: SignInSheetScreen

    init(_ screen: SignInSheetScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { SignInSheetScreenUiController(screen: screen) }
    }
}
