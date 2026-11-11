import KMP
import MapKit
import SwiftUI

/// The map's MapKit view. It follows the user's location while `map.followsUser` is set, and reports when the user pans it.
struct MapView: UIViewRepresentable {
    // Businesses and transit, as the Android map hides them
    private static let hiddenCategories: [MKPointOfInterestCategory] = [
        .atm, .bakery, .bank, .beauty, .brewery, .cafe, .carRental, .distillery, .evCharger, .fitnessCenter, .foodMarket,
        .gasStation, .hotel, .laundry, .nightlife, .parking, .pharmacy, .restaurant, .store, .winery, .automotiveRepair,
        .animalService, .spa, .publicTransport, .airport
    ]

    // The covered area, with a margin around its corners
    private static let overviewPadding = 1.1

    private let map: RouteMapState
    private let bottomMargin: CGFloat
    private let onPanned: () -> Void

    init(map: RouteMapState, bottomMargin: CGFloat, onPanned: @escaping () -> Void) {
        self.map = map
        self.bottomMargin = bottomMargin
        self.onPanned = onPanned
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onPanned: onPanned)
    }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        let configuration = MKStandardMapConfiguration(elevationStyle: .flat, emphasisStyle: .muted)
        configuration.pointOfInterestFilter = MKPointOfInterestFilter(excluding: Self.hiddenCategories)
        mapView.preferredConfiguration = configuration
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = map.showsUserLocation
        mapView.setRegion(Self.region(for: map.overview), animated: false)
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        context.coordinator.onPanned = onPanned
        mapView.showsUserLocation = map.showsUserLocation
        mapView.layoutMargins.bottom = bottomMargin

        // The map doesn't rotate with the heading: `.follow`, not `.followWithHeading`
        let mode: MKUserTrackingMode = map.followsUser && map.showsUserLocation ? .follow : .none
        if mode != mapView.userTrackingMode {
            context.coordinator.isChangingMode = true
            mapView.setUserTrackingMode(mode, animated: true)
            context.coordinator.isChangingMode = false
        }
    }

    private static func region(for overview: DomainGeoBounds) -> MKCoordinateRegion {
        let southWest = overview.southWest
        let northEast = overview.northEast
        let center = CLLocationCoordinate2D(
            latitude: (southWest.latitude + northEast.latitude) / 2,
            longitude: (southWest.longitude + northEast.longitude) / 2
        )
        let span = MKCoordinateSpan(
            latitudeDelta: (northEast.latitude - southWest.latitude) * overviewPadding,
            longitudeDelta: (northEast.longitude - southWest.longitude) * overviewPadding
        )
        return MKCoordinateRegion(center: center, span: span)
    }

    final class Coordinator: NSObject, MKMapViewDelegate {
        var onPanned: () -> Void
        var isChangingMode = false

        init(onPanned: @escaping () -> Void) {
            self.onPanned = onPanned
        }

        func mapView(_ mapView: MKMapView, didChange mode: MKUserTrackingMode, animated: Bool) {
            // MapKit stops following when the user pans the map
            if mode == .none, !isChangingMode {
                onPanned()
            }
        }
    }
}
