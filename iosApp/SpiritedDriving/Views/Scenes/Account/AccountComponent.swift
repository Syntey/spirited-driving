import KMP
import SwiftUI

struct AccountComponent: View {
    private let screen: AccountScreen
    @State private var model: AccountComponentModel

    init(_ screen: AccountScreen) {
        self.screen = screen
        _model = State(initialValue: AccountComponentModel(screen))
    }

    var body: some View {
        ComposeView { AccountScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(Localizable.account_screen_title.localized)
            .navigationBarTitleDisplayMode(.inline)
            .alert(Localizable.account_delete_dialog_title.localized, isPresented: $model.isDeleteDialogPresented) {
                Button(Localizable.generic_cancel.localized, role: .cancel) {}
                Button(Localizable.account_delete.localized, role: .destructive) { model.onDeleteConfirm() }
            } message: {
                Text(Localizable.account_delete_dialog_message.localized)
            }
    }
}
