import KMP
import SwiftUI

struct TripHistoryComponent: View {
    private let screen: TripHistoryScreen
    @State private var model: TripHistoryComponentModel

    init(_ screen: TripHistoryScreen) {
        self.screen = screen
        _model = State(initialValue: TripHistoryComponentModel(screen))
    }

    var body: some View {
        ComposeView { TripHistoryScreenUiController(screen: screen) }
            .composeScreen()
            .navigationTitle(screen.title.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if model.canClear {
                    ToolbarItem(placement: .topBarTrailing) {
                        Menu {
                            Button(Localizable.trip_history_clear.localized, role: .destructive) {
                                model.onClearClick()
                            }
                        } label: {
                            Image(systemName: "ellipsis")
                        }
                    }
                }
            }
            .alert(Localizable.trip_history_clear_title.localized,
                   isPresented: $model.isClearConfirmationPresented) {
                Button(Localizable.generic_cancel.localized, role: .cancel) {}
                Button(Localizable.trip_history_clear_confirm.localized, role: .destructive) { model.onClearConfirm() }
            } message: {
                Text(Localizable.trip_history_clear_text.localized)
            }
            .sheet(isPresented: $model.isRatingSheetPresented) {
                ComposeView {
                    RatingSheetUiController(screen: screen) { height in
                        model.ratingSheetHeight = CGFloat(height.doubleValue)
                    }
                }
                .presentationDetents([.height(model.ratingSheetHeight)])
                .presentationDragIndicator(.visible)
            }
    }
}
