import KMP
import SwiftUI

struct HomeTabNavigationComponent<Model: HomeTabNavigationComponentModelProtocol>: View {
    @State var model: Model

    var body: some View {
        DecomposeNavigationStack(
            kotlinStack: model.stack,
            setPath: model.navigate
        ) { child in
            switch onEnum(of: child) {
            case let .first(entry):
                FirstComponent(entry.screen)
            case let .second(entry):
                SecondComponent(entry.screen)
            case let .third(entry):
                ThirdComponent(entry.screen)
            }
        }
        .sheet(item: $model.sheetItem) { item in
            switch onEnum(of: item.instance) {
            case let .picker(instance):
                PickerComponent(instance.screen)
                    .presentationDetents(.init([.medium]))
            }
        }
    }
}
