import KMP
import Observation
import SwiftUI
import UIKit

/// KMP deviation: Conforms to `AnyObject` instead of FuturedKit's `ComponentModel` protocol
/// because navigation and event handling are managed by KMP Decompose, not a Swift Coordinator.
protocol MapComponentModelProtocol: AnyObject {
    var map: RouteMapState { get }
    var locateButton: LocateButton { get }
    /// The height of the collapsed sheet, as its Compose content measures it.
    var collapsedSheetHeight: CGFloat { get }
    /// The sheet's detent, which the view state owns: focusing the field and closing the search move it, and a drag sets it.
    var sheetDetent: PresentationDetent { get set }
    /// Where the presented sheet's top edge is, from the top of the screen; 0 until measured. Its content is laid out at the
    /// tallest detent, so its height says nothing about how much of the map the sheet covers.
    var sheetTop: CGFloat { get set }
    var routeLines: [RouteLine] { get }
    var routeBounds: DomainGeoBounds? { get }
    var showsMapButtons: Bool { get }
    var driveDialog: DriveDialog? { get }
    /// Whether the drive dialog's alert is shown. SwiftUI sets it to false when the alert goes away; that dismisses the dialog.
    var isDriveDialogPresented: Bool { get set }
    /// The name dialog, shown when the destination's name is taken; nil when there's none.
    var nameDialog: RouteNameDialog? { get }
    /// Whether the name dialog's alert is shown. SwiftUI sets it to false when the alert goes away; that dismisses the dialog.
    var isNameDialogPresented: Bool { get set }
    var nameDialogMessage: String { get }
    var showsReminder: Bool { get }
    /// The number of the reminded route; null without a reminder.
    var reminderNumber: Int? { get }
    var reminderText: String { get }

    func sheetViewController() -> UIViewController
    func onLocateClick()
    func onMapPanned()
    func onMapLongPress(_ point: DomainGeoPoint)
    func onProfileClick()
    func onRouteClick(_ id: String)
    func onFirstDriveConfirm()
    func onMapsAppStoreClick()
    func onReminderClick()
    func onReminderDismiss()
    func onRouteNameSave(_ name: String)
    func onRouteNameDismiss()
}

@Observable
final class MapComponentModel: MapComponentModelProtocol {

    // MARK: Public stored properties

    // Until the sheet's content is measured: the search field and its padding
    private(set) var collapsedSheetHeight: CGFloat = 82
    var sheetTop: CGFloat = 0

    // MARK: Public computed properties

    var map: RouteMapState { viewState.map }
    var locateButton: LocateButton { viewState.locateButton }
    var routeLines: [RouteLine] { Array(viewState.routeLines) }
    var routeBounds: DomainGeoBounds? { viewState.routeBounds }
    var showsMapButtons: Bool { viewState.showsMapButtons }
    var driveDialog: DriveDialog? { viewState.driveDialog }
    var showsReminder: Bool { viewState.showsReminder }
    var reminderNumber: Int? { viewState.reminder.map { Int($0.question.routeNumber) } }
    var reminderText: String { viewState.reminderText?.localized() ?? "" }
    var isDriveDialogPresented: Bool {
        get { viewState.driveDialog != nil }
        set {
            if !newValue, viewState.driveDialog != nil {
                screen.actions.onDriveDialogDismiss()
            }
        }
    }
    var nameDialog: RouteNameDialog? { viewState.nameDialog }
    var nameDialogMessage: String { viewState.nameDialog?.message.localized() ?? "" }
    var isNameDialogPresented: Bool {
        get { viewState.nameDialog != nil }
        set {
            if !newValue, viewState.nameDialog != nil {
                screen.actions.onRouteNameDismiss()
            }
        }
    }
    var sheetDetent: PresentationDetent {
        get {
            switch viewState.sheetHeight {
            case .collapsed:
                .height(collapsedSheetHeight)
            case .half:
                .medium
            case .full:
                .large
            }
        }
        set {
            switch newValue {
            case .large:
                screen.actions.onSheetHeightChange(height: .full)
            case .medium:
                screen.actions.onSheetHeightChange(height: .half)
            default:
                screen.actions.onSheetHeightChange(height: .collapsed)
            }
        }
    }

    // MARK: Private stored properties

    private var viewState: TripPlanViewState
    @ObservationIgnored private let screen: TripPlanScreen

    // MARK: Private @ObservationIgnored properties

    @ObservationIgnored private var stateTask: Task<Void, Never>?

    // MARK: Init / Deinit

    init(_ screen: TripPlanScreen) {
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

    func sheetViewController() -> UIViewController {
        TripPlanSheetUiController(screen: screen) { [weak self] height in
            self?.collapsedSheetHeight = CGFloat(height.doubleValue)
        }
    }

    func onLocateClick() {
        screen.actions.onLocateClick()
    }

    func onMapPanned() {
        screen.actions.onMapPanned()
    }

    func onMapLongPress(_ point: DomainGeoPoint) {
        screen.actions.onMapLongPress(point: point)
    }

    func onProfileClick() {
        screen.actions.onProfileClick()
    }

    func onRouteClick(_ id: String) {
        screen.actions.onRouteClick(routeId: id)
    }

    func onFirstDriveConfirm() {
        screen.actions.onFirstDriveConfirm()
    }

    func onMapsAppStoreClick() {
        screen.actions.onMapsAppStoreClick()
    }

    func onReminderClick() {
        screen.actions.onReminderClick()
    }

    func onReminderDismiss() {
        screen.actions.onReminderDismiss()
    }

    func onRouteNameSave(_ name: String) {
        screen.actions.onRouteNameSave(name: name)
    }

    func onRouteNameDismiss() {
        screen.actions.onRouteNameDismiss()
    }
}

#if DEBUG
@Observable
final class MapComponentModelMock: MapComponentModelProtocol {
    var map = RouteMapState(
        showsUserLocation: true,
        followsUser: true,
        userLocation: nil,
        overview: DomainGeoBounds(
            southWest: DomainGeoPoint(latitude: 48.55, longitude: 12.09),
            northEast: DomainGeoPoint(latitude: 51.06, longitude: 18.86)
        ),
        coveredArea: [
            DomainGeoPolygon(
                outline: [
                    DomainGeoPoint(latitude: 48.55, longitude: 12.09),
                    DomainGeoPoint(latitude: 48.55, longitude: 18.86),
                    DomainGeoPoint(latitude: 51.06, longitude: 18.86),
                    DomainGeoPoint(latitude: 51.06, longitude: 12.09),
                    DomainGeoPoint(latitude: 48.55, longitude: 12.09)
                ],
                holes: []
            )
        ],
        overviewRequest: 0,
        destination: nil,
        start: nil
    )
    var locateButton = LocateButton.following
    var collapsedSheetHeight: CGFloat = 82
    var sheetDetent: PresentationDetent = .height(82)
    var sheetTop: CGFloat = 0
    var routeLines: [RouteLine] = []
    var routeBounds: DomainGeoBounds?
    var showsMapButtons = true
    var driveDialog: DriveDialog?
    var isDriveDialogPresented = false
    var nameDialog: RouteNameDialog?
    var nameDialogMessage = ""
    var isNameDialogPresented = false
    var showsReminder = true
    var reminderNumber: Int? = 1
    var reminderText = "Karlštejn Castle · Yesterday"

    func sheetViewController() -> UIViewController {
        UIViewController()
    }

    func onLocateClick() {}
    func onMapPanned() {}
    func onMapLongPress(_ point: DomainGeoPoint) {}
    func onProfileClick() {}
    func onRouteClick(_ id: String) {}
    func onFirstDriveConfirm() {}
    func onMapsAppStoreClick() {}
    func onReminderClick() {}
    func onReminderDismiss() {}
    func onRouteNameSave(_ name: String) {}
    func onRouteNameDismiss() {}
}
#endif
