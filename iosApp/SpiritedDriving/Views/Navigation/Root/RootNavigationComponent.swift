import KMP
import SwiftUI

struct RootNavigationComponent<Model: RootNavigationComponentModelProtocol>: View {
    @State var model: Model

    var body: some View {
        Group {
            if let navigationEntry = model.slotChild {
                switch onEnum(of: navigationEntry) {
                case let .onboarding(entry):
                    OnboardingComponent(entry.screen).id(entry.iosViewId)
                case let .map(entry):
                    MapComponent(model: MapComponentModel(entry.screen), profile: $model.profileSheetItem)
                        .id(entry.iosViewId)
                }
            }
        }
    }
}
