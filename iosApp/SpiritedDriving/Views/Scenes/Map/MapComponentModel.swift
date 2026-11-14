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

    func sheetViewController() -> UIViewController
    func onLocateClick()
    func onMapPanned()
    func onMapLongPress(_ point: DomainGeoPoint)
    func onProfileClick()
    func onRouteClick(_ id: String)
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

    func sheetViewController() -> UIViewController {
        UIViewController()
    }

    func onLocateClick() {}
    func onMapPanned() {}
    func onMapLongPress(_ point: DomainGeoPoint) {}
    func onProfileClick() {}
    func onRouteClick(_ id: String) {}
}
#endif
