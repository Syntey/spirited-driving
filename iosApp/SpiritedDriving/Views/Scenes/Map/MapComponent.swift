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
                // Inside the sheet: an alert on the view under a presented sheet wouldn't show
                .alert(driveDialogTitle,
                       isPresented: $model.isDriveDialogPresented,
                       presenting: model.driveDialog) { dialog in
                    driveDialogButtons(dialog)
                } message: { dialog in
                    Text(driveDialogMessage(dialog))
                }
        }
    }

    // MARK: Private views

    @ViewBuilder
    private func driveDialogButtons(_ dialog: DriveDialog) -> some View {
        Button(Localizable.generic_cancel.localized, role: .cancel) {}
        switch dialog {
        case .firstDrive:
            Button(Localizable.drive_first_open_apple.localized) { model.onFirstDriveConfirm() }
                .keyboardShortcut(.defaultAction)
        case .mapsAppMissing:
            Button(Localizable.drive_missing_store_apple.localized) { model.onMapsAppStoreClick() }
                .keyboardShortcut(.defaultAction)
        }
    }

    private func driveDialogMessage(_ dialog: DriveDialog) -> String {
        switch dialog {
        case .firstDrive:
            Localizable.drive_first_message_apple.localized
        case .mapsAppMissing:
            Localizable.drive_missing_message_apple.localized
        }
    }

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

    // Without a dialog the alert isn't shown, so its title is only read while one is
    private var driveDialogTitle: String {
        switch model.driveDialog {
        case .mapsAppMissing:
            Localizable.drive_missing_title_apple.localized
        case .firstDrive, nil:
            Localizable.drive_first_title_apple.localized
        }
    }

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
