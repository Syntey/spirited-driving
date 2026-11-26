import KMP
import Observation

@Observable
final class SavedRoutesComponentModel {

    // MARK: Public computed properties

    var renameDialog: RenameDialog? { viewState.renameDialog }

    /// Whether the rename alert is shown. SwiftUI sets it to false when the alert goes away; that dismisses the dialog.
    var isRenameDialogPresented: Bool {
        get { viewState.renameDialog != nil }
        set {
            if !newValue, viewState.renameDialog != nil {
                screen.actions.onRenameDismiss()
            }
        }
    }

    /// Whether the delete alert is shown. SwiftUI sets it to false when the alert goes away; that dismisses the confirmation.
    var isDeleteConfirmationPresented: Bool {
        get { viewState.isDeleteConfirmationShown }
        set {
            if !newValue, viewState.isDeleteConfirmationShown {
                screen.actions.onDeleteDismiss()
            }
        }
    }

    // MARK: Private stored properties

    private var viewState: SavedRoutesViewState
    @ObservationIgnored private let screen: SavedRoutesScreen

    // MARK: Private @ObservationIgnored properties

    @ObservationIgnored private var stateTask: Task<Void, Never>?

    // MARK: Init / Deinit

    init(_ screen: SavedRoutesScreen) {
        self.screen = screen
        viewState = screen.viewState.value

        // Safe: SKIE's StateFlow replays the current value to new collectors,
        // so no emissions are lost between the synchronous read above and this Task.
        stateTask = Task { [weak self] in
            for await state in screen.viewState {
                self?.viewState = state
            }
        }
    }

    deinit {
        stateTask?.cancel()
    }

    // MARK: Public functions

    func onRenameSave(_ name: String) {
        screen.actions.onRenameSave(name: name)
    }

    func onRenameDismiss() {
        screen.actions.onRenameDismiss()
    }

    func onDeleteConfirm() {
        screen.actions.onDeleteConfirm()
    }
}
