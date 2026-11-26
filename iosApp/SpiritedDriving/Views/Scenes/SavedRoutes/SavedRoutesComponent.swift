import KMP
import SwiftUI

struct SavedRoutesComponent: View {
    private let screen: SavedRoutesScreen
    @State private var model: SavedRoutesComponentModel
    // The name the rename alert's field holds, set to the current name as the alert appears
    @State private var nameText = ""

    init(_ screen: SavedRoutesScreen) {
        self.screen = screen
        _model = State(initialValue: SavedRoutesComponentModel(screen))
    }

    var body: some View {
        NavigationStack {
            contentView
        }
    }

    private var contentView: some View {
        ComposeView { SavedRoutesScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(screen.title.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(role: .close) {
                        screen.actions.onClose()
                    }
                }
            }
            .alert(Localizable.saved_routes_rename_title.localized,
                   isPresented: $model.isRenameDialogPresented,
                   presenting: model.renameDialog) { dialog in
                TextField("", text: $nameText)
                Button(Localizable.generic_cancel.localized, role: .cancel) { model.onRenameDismiss() }
                Button(Localizable.planning_save.localized) { model.onRenameSave(nameText) }
                    .disabled(!dialog.canSave(name: nameText))
            } message: { dialog in
                Text(dialog.isTaken(name: nameText) ? Localizable.saved_routes_name_taken.localized : "")
            }
            .onChange(of: model.isRenameDialogPresented) { _, presented in
                if presented { nameText = model.renameDialog?.currentName ?? "" }
            }
            .alert(Localizable.saved_routes_delete_title.localized,
                   isPresented: $model.isDeleteConfirmationPresented) {
                Button(Localizable.generic_cancel.localized, role: .cancel) {}
                Button(Localizable.saved_routes_delete.localized, role: .destructive) { model.onDeleteConfirm() }
            } message: {
                Text(Localizable.saved_routes_delete_text.localized)
            }
    }
}
