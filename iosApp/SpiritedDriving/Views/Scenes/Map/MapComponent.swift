import KMP
import SwiftUI

private enum Layout {
    static let initialSheetHeight: CGFloat = 82
    static let sheetBottomInset: CGFloat = 8
    static let buttonSize: CGFloat = 48
    static let buttonSpacing: CGFloat = 8
    static let buttonTopPadding: CGFloat = 8
    static let buttonTrailingPadding: CGFloat = 16
    static let iconSize: CGFloat = 20
}

struct MapComponent<Model: MapComponentModelProtocol>: View {
    @State var model: Model
    // Until the sheet's content is measured: the search field and its padding
    @State private var sheetHeight: CGFloat = Layout.initialSheetHeight

    init(model: Model) {
        _model = State(initialValue: model)
    }

    var body: some View {
        ZStack(alignment: .topTrailing) {
            MapView(map: model.map, bottomMargin: sheetHeight + Layout.sheetBottomInset, onPanned: model.onMapPanned)
                .ignoresSafeArea()
            buttons
        }
        .sheet(isPresented: .constant(true)) {
            ComposeView { model.sheetViewController { height in sheetHeight = height } }
                .presentationDetents([.height(sheetHeight)])
                .presentationDragIndicator(.visible)
                .presentationBackgroundInteraction(.enabled)
                .interactiveDismissDisabled()
        }
    }

    // MARK: Private views

    private var buttons: some View {
        VStack(spacing: Layout.buttonSpacing) {
            glassButton(
                systemName: "person.fill",
                color: .primary,
                label: Localizable.map_profile.localized,
                action: model.onProfileClick
            )
            glassButton(
                systemName: locateSystemName,
                color: locateColor,
                label: locateLabel,
                action: model.onLocateClick
            )
        }
        .padding(.top, Layout.buttonTopPadding)
        .padding(.trailing, Layout.buttonTrailingPadding)
    }

    private func glassButton(
        systemName: String,
        color: Color,
        label: String,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: Layout.iconSize, weight: .semibold))
                .foregroundStyle(color)
                .frame(width: Layout.buttonSize, height: Layout.buttonSize)
        }
        .glassEffect(.regular.interactive(), in: .circle)
        .accessibilityLabel(label)
    }

    // MARK: Private computed properties

    private var locateSystemName: String {
        switch model.locateButton {
        case .following:
            "location.fill"
        case .notFollowing:
            "location"
        case .noAccess:
            "location.slash"
        }
    }

    private var locateColor: Color {
        switch model.locateButton {
        case .following, .notFollowing:
            .accentColor
        case .noAccess:
            .secondary
        }
    }

    private var locateLabel: String {
        switch model.locateButton {
        case .following:
            Localizable.map_locate_following.localized
        case .notFollowing:
            Localizable.map_locate_show.localized
        case .noAccess:
            Localizable.map_locate_no_access.localized
        }
    }
}

#if DEBUG
#Preview {
    MapComponent(model: MapComponentModelMock())
}
#endif
