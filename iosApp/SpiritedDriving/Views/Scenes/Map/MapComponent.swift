import KMP
import SwiftUI
import UIKit

private enum Layout {
    static let sheetBottomInset: CGFloat = 8
    static let buttonSize: CGFloat = 48
    static let buttonSpacing: CGFloat = 8
    static let buttonTopPadding: CGFloat = 8
    static let buttonTrailingPadding: CGFloat = 16
    static let iconSize: CGFloat = 20
    static let reminderHeight: CGFloat = 56
    static let reminderSheetGap: CGFloat = 12
    static let reminderHorizontalPadding: CGFloat = 16
    static let reminderSpacing: CGFloat = 12
    static let reminderBadgeSize: CGFloat = 32
}

struct MapComponent<Model: MapComponentModelProtocol>: View {
    @State var model: Model
    @Binding private var sheet: DecomposeSlotItem<RootSheetChild>?
    @Environment(\.colorScheme) private var colorScheme
    // The name the name dialog's field holds, set to the suggestion as the dialog appears
    @State private var nameText = ""

    init(model: Model, sheet: Binding<DecomposeSlotItem<RootSheetChild>?>) {
        _model = State(initialValue: model)
        _sheet = sheet
    }

    var body: some View {
        ZStack(alignment: .topTrailing) {
            MapView(
                map: model.map,
                routeLines: model.routeLines,
                routeBounds: model.routeBounds,
                bottomMargin: model.collapsedSheetHeight + Layout.sheetBottomInset + reminderInset,
                sheetTop: model.sheetTop,
                isSheetLarge: model.sheetDetent == .large,
                onPanned: model.onMapPanned,
                onLongPress: model.onMapLongPress,
                onRouteClick: model.onRouteClick
            )
            .ignoresSafeArea()
            buttons
            // Left out while hidden, not only faded, so VoiceOver doesn't find a reminder that isn't there
            if model.showsReminder {
                reminderBubble
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut, value: model.showsReminder)
        .sheet(isPresented: .constant(true)) {
            ComposeView { model.sheetViewController() }
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).minY } action: { model.sheetTop = $0 }
                .presentationDetents([.height(model.collapsedSheetHeight), .medium, .large], selection: $model.sheetDetent)
                .presentationDragIndicator(.visible)
                .presentationBackgroundInteraction(.enabled)
                .interactiveDismissDisabled()
                .sheet(item: $sheet) { item in
                    rootSheetContent(item.instance)
                }
                // Inside the sheet: an alert on the view under a presented sheet wouldn't show
                .alert(driveDialogTitle,
                       isPresented: $model.isDriveDialogPresented,
                       presenting: model.driveDialog) { dialog in
                    driveDialogButtons(dialog)
                } message: { dialog in
                    Text(driveDialogMessage(dialog))
                }
                .alert(Localizable.saved_routes_name_title.localized,
                       isPresented: $model.isNameDialogPresented,
                       presenting: model.nameDialog) { dialog in
                    TextField("", text: $nameText)
                    Button(Localizable.generic_cancel.localized, role: .cancel) { model.onRouteNameDismiss() }
                    Button(Localizable.planning_save.localized) { model.onRouteNameSave(nameText) }
                        .disabled(!dialog.canSave(name: nameText))
                } message: { _ in
                    Text(model.nameDialogMessage)
                }
                .onChange(of: model.isNameDialogPresented) { _, presented in
                    if presented { nameText = model.nameDialog?.suggestion ?? "" }
                }
        }
    }

    // MARK: Private views

    @ViewBuilder
    private func rootSheetContent(_ child: RootSheetChild) -> some View {
        switch onEnum(of: child) {
        case let .profile(entry):
            ProfileNavigationComponent(navHost: entry.navHost)
                .presentationDetents([.large])
        case let .signIn(entry):
            SignInSheetComponent(entry.screen)
                .presentationDetents([.medium])
        case let .savedRoutes(entry):
            SavedRoutesComponent(entry.screen)
                .presentationDetents([.large])
        }
    }

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

    // The reminder sits above the sheet, so the map's logo and legal notice move up by its height and the gap
    private var reminderInset: CGFloat {
        model.showsReminder ? Layout.reminderHeight + Layout.reminderSheetGap : 0
    }

    private var reminderBubble: some View {
        HStack(spacing: Layout.reminderSpacing) {
            Button(action: model.onReminderClick) {
                HStack(spacing: Layout.reminderSpacing) {
                    if let number = model.reminderNumber {
                        reminderBadge(number: number)
                    }
                    VStack(alignment: .leading, spacing: 0) {
                        // The question never truncates; the destination's line does
                        Text(Localizable.drive_feedback_question_unnamed.localized)
                            .font(.subheadline.weight(.semibold))
                            .lineLimit(1)
                            .fixedSize(horizontal: true, vertical: false)
                        Text(model.reminderText)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                            .truncationMode(.tail)
                    }
                }
                .contentShape(.rect)
            }
            .buttonStyle(.plain)
            Button(action: model.onReminderDismiss) {
                MR.images().close.image
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: Layout.iconSize, height: Layout.iconSize)
                    .foregroundStyle(.secondary)
                    .frame(width: Layout.buttonSize, height: Layout.buttonSize)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Localizable.generic_close.localized)
        }
        .frame(height: Layout.reminderHeight)
        .padding(.horizontal, Layout.reminderHorizontalPadding)
        .frame(maxWidth: .infinity)
        .glassEffect(.regular.interactive(), in: .capsule)
        .padding(.bottom, model.collapsedSheetHeight + Layout.reminderSheetGap)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
        .ignoresSafeArea(edges: .bottom)
    }

    // The route's badge in its colour, with the number in the text colour the route labels use
    private func reminderBadge(number: Int) -> some View {
        let traits = UITraitCollection(userInterfaceStyle: colorScheme == .dark ? .dark : .light)
        let route = MapRoutes.lineColor(for: number).resolvedColor(with: traits)
        return Text("\(number)")
            .font(.subheadline.weight(.semibold).monospacedDigit())
            .foregroundStyle(Color(uiColor: MapRouteLabels.textColor(on: route)))
            .frame(width: Layout.reminderBadgeSize, height: Layout.reminderBadgeSize)
            .background(Color(uiColor: route), in: Circle())
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
    MapComponent(model: MapComponentModelMock(), sheet: .constant(nil))
}
#endif
