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

    func sheetViewController() -> UIViewController
    func onLocateClick()
    func onMapPanned()
    func onProfileClick()
}

@Observable
final class MapComponentModel: MapComponentModelProtocol {

    // MARK: Public stored properties

    // Until the sheet's content is measured: the search field and its padding
    private(set) var collapsedSheetHeight: CGFloat = 82

    // MARK: Public computed properties

    var map: RouteMapState { viewState.map }
    var locateButton: LocateButton { viewState.locateButton }
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

    func onProfileClick() {
        screen.actions.onProfileClick()
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
        destination: nil
    )
    var locateButton = LocateButton.following
    var collapsedSheetHeight: CGFloat = 82
    var sheetDetent: PresentationDetent = .height(82)

    func sheetViewController() -> UIViewController {
        UIViewController()
    }

    func onLocateClick() {}
    func onMapPanned() {}
    func onProfileClick() {}
}
#endif
