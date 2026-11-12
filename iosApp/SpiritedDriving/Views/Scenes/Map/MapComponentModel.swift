import KMP
import Observation
import UIKit

/// KMP deviation: Conforms to `AnyObject` instead of FuturedKit's `ComponentModel` protocol
/// because navigation and event handling are managed by KMP Decompose, not a Swift Coordinator.
protocol MapComponentModelProtocol: AnyObject {
    var map: RouteMapState { get }
    var locateButton: LocateButton { get }

    func sheetViewController(onHeightChanged: @escaping (CGFloat) -> Void) -> UIViewController
    func onLocateClick()
    func onMapPanned()
    func onProfileClick()
}

@Observable
final class MapComponentModel: MapComponentModelProtocol {

    // MARK: Public computed properties

    var map: RouteMapState { viewState.map }
    var locateButton: LocateButton { viewState.locateButton }

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

    func sheetViewController(onHeightChanged: @escaping (CGFloat) -> Void) -> UIViewController {
        TripPlanSheetUiController(screen: screen) { height in
            onHeightChanged(CGFloat(height.doubleValue))
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
        overviewRequest: 0
    )
    var locateButton = LocateButton.following

    func sheetViewController(onHeightChanged: @escaping (CGFloat) -> Void) -> UIViewController {
        UIViewController()
    }

    func onLocateClick() {}
    func onMapPanned() {}
    func onProfileClick() {}
}
#endif
