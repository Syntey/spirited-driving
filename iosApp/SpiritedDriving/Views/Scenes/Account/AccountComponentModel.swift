import KMP
import Observation

@Observable
final class AccountComponentModel {

    // MARK: Public computed properties

    /// Whether the delete alert is shown. SwiftUI sets it to false when the alert goes away; that dismisses the dialog.
    var isDeleteDialogPresented: Bool {
        get { viewState.isDeleteDialogShown }
        set {
            if !newValue, viewState.isDeleteDialogShown {
                screen.actions.onDeleteDialogDismiss()
            }
        }
    }

    // MARK: Private stored properties

    private var viewState: AccountViewState
    @ObservationIgnored private let screen: AccountScreen

    // MARK: Private @ObservationIgnored properties

    @ObservationIgnored private var stateTask: Task<Void, Never>?

    // MARK: Init / Deinit

    init(_ screen: AccountScreen) {
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

    func onDeleteConfirm() {
        screen.actions.onDeleteConfirm()
    }
}
