import KMP
import SwiftUI

private enum Layout {
    static let sheetBottomInset: CGFloat = 8
    static let buttonSize: CGFloat = 48
    static let buttonSpacing: CGFloat = 8
    static let buttonTopPadding: CGFloat = 8
    static let buttonTrailingPadding: CGFloat = 16
    static let iconSize: CGFloat = 20
}

struct MapComponent<Model: MapComponentModelProtocol>: View {
    @State var model: Model
    @Binding private var profile: DecomposeSlotItem<ProfileNavHost>?

    init(model: Model, profile: Binding<DecomposeSlotItem<ProfileNavHost>?>) {
        _model = State(initialValue: model)
        _profile = profile
    }

    var body: some View {
        ZStack(alignment: .topTrailing) {
            MapView(
                map: model.map,
                routeLines: model.routeLines,
                routeBounds: model.routeBounds,
                bottomMargin: model.collapsedSheetHeight + Layout.sheetBottomInset,
                sheetTop: model.sheetTop,
                isSheetLarge: model.sheetDetent == .large,
                onPanned: model.onMapPanned,
                onLongPress: model.onMapLongPress,
                onRouteClick: model.onRouteClick
            )
            .ignoresSafeArea()
            buttons
        }
        .sheet(isPresented: .constant(true)) {
            ComposeView { model.sheetViewController() }
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).minY } action: { model.sheetTop = $0 }
                .presentationDetents([.height(model.collapsedSheetHeight), .medium, .large], selection: $model.sheetDetent)
                .presentationDragIndicator(.visible)
                .presentationBackgroundInteraction(.enabled)
                .interactiveDismissDisabled()
                .sheet(item: $profile) { item in
                    ProfileNavigationComponent(navHost: item.instance)
                        .presentationDetents([.large])
                }
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
        .opacity(model.showsMapButtons ? 1 : 0)
        .allowsHitTesting(model.showsMapButtons)
        .animation(.easeInOut, value: model.showsMapButtons)
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
    MapComponent(model: MapComponentModelMock(), profile: .constant(nil))
}
#endif
