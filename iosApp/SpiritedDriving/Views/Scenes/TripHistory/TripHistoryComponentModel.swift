import KMP
import Observation
import SwiftUI

@Observable
final class TripHistoryComponentModel {

    // MARK: Public computed properties

    /// Whether the bar offers Clear trip history: there are drives to clear.
    var canClear: Bool { viewState.canClear }

    /// Whether the clear alert is shown. SwiftUI sets it to false when the alert goes away; that dismisses the confirmation.
    var isClearConfirmationPresented: Bool {
        get { viewState.isClearConfirmationShown }
        set {
            if !newValue, viewState.isClearConfirmationShown {
                screen.actions.onClearDismiss()
            }
        }
    }

    /// Whether the rating sheet is shown. SwiftUI sets it to false when the sheet goes away; that closes the rating, as Done does.
    var isRatingSheetPresented: Bool {
        get { viewState.ratingSheet != nil }
        set {
            if !newValue, viewState.ratingSheet != nil {
                screen.actions.onRatingSheetClose()
            }
        }
    }

    /// The height of the rating sheet's content, which the sheet fits. The content reports it once it's measured.
    var ratingSheetHeight: CGFloat = 300

    // MARK: Private stored properties

    private var viewState: TripHistoryViewState
    @ObservationIgnored private let screen: TripHistoryScreen

    // MARK: Private @ObservationIgnored properties

    @ObservationIgnored private var stateTask: Task<Void, Never>?

    // MARK: Init / Deinit

    init(_ screen: TripHistoryScreen) {
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

    func onClearClick() {
        screen.actions.onClearClick()
    }

    func onClearConfirm() {
        screen.actions.onClearConfirm()
    }
}
