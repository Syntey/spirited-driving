import KMP
import MapKit
import SwiftUI

/// The map's MapKit view. It follows the user's location while `map.followsUser` is set, and reports when the user pans it.
/// Touching and holding it picks a point, with a haptic tap. While planning, it frames the routes above the sheet, and a tap
/// on a route line picks that route.
struct MapView: UIViewRepresentable {
    // Businesses and transit, as the Android map hides them
    private static let hiddenCategories: [MKPointOfInterestCategory] = [
        .atm, .bakery, .bank, .beauty, .brewery, .cafe, .carRental, .distillery, .evCharger, .fitnessCenter, .foodMarket,
        .gasStation, .hotel, .laundry, .nightlife, .parking, .pharmacy, .restaurant, .store, .winery, .automotiveRepair,
        .animalService, .spa, .publicTransport, .airport
    ]

    // The covered area, with a margin around its corners
    private static let overviewPadding = 1.1

    // The theme's `scrim` at 20% light and 50% dark, and `secondary` from core/ui's Colors.kt
    private static let dimColor = UIColor(resource: .coveredAreaDim)
    private static let borderColor = UIColor(resource: .coveredAreaBorder)
    private static let borderWidth: CGFloat = 1.5

    private let map: RouteMapState
    private let routeLines: [RouteLine]
    private let routeBounds: DomainGeoBounds?
    private let bottomMargin: CGFloat
    private let sheetTop: CGFloat
    private let isSheetLarge: Bool
    private let onPanned: () -> Void
    private let onLongPress: (DomainGeoPoint) -> Void
    private let onRouteClick: (String) -> Void

    init(
        map: RouteMapState,
        routeLines: [RouteLine],
        routeBounds: DomainGeoBounds?,
        bottomMargin: CGFloat,
        sheetTop: CGFloat,
        isSheetLarge: Bool,
        onPanned: @escaping () -> Void,
        onLongPress: @escaping (DomainGeoPoint) -> Void,
        onRouteClick: @escaping (String) -> Void
    ) {
        self.map = map
        self.routeLines = routeLines
        self.routeBounds = routeBounds
        self.bottomMargin = bottomMargin
        self.sheetTop = sheetTop
        self.isSheetLarge = isSheetLarge
        self.onPanned = onPanned
        self.onLongPress = onLongPress
        self.onRouteClick = onRouteClick
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onPanned: onPanned, onLongPress: onLongPress, onRouteClick: onRouteClick)
    }

    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        let configuration = MKStandardMapConfiguration(elevationStyle: .flat, emphasisStyle: .muted)
        configuration.pointOfInterestFilter = MKPointOfInterestFilter(excluding: Self.hiddenCategories)
        mapView.preferredConfiguration = configuration
        mapView.delegate = context.coordinator
        let longPress = UILongPressGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleLongPress(_:)))
        mapView.addGestureRecognizer(longPress)
        let tap = UITapGestureRecognizer(target: context.coordinator, action: #selector(Coordinator.handleTap(_:)))
        tap.delegate = context.coordinator
        mapView.addGestureRecognizer(tap)
        // Its own margins only: inherited ones grow while the sheet is at full height, and MapKit fits regions inside the
        // margins, so a destination chosen then would show zoomed out at the bottom edge
        mapView.preservesSuperviewLayoutMargins = false
        mapView.showsUserLocation = map.showsUserLocation
        mapView.setRegion(Self.region(for: map.overview), animated: false)
        context.coordinator.shownOverviewRequest = map.overviewRequest
        context.coordinator.showCoveredArea(map.coveredArea, on: mapView)
        context.coordinator.showDestination(map.destination, on: mapView)

        // The renderers take their colours from the trait collection when created, so the overlays are re-added on change
        let coordinator = context.coordinator
        mapView.registerForTraitChanges([UITraitUserInterfaceStyle.self]) { (mapView: MKMapView, _) in
            coordinator.showCoveredArea(coordinator.coveredArea, on: mapView)
        }
        return mapView
    }

    func updateUIView(_ mapView: MKMapView, context: Context) {
        let coordinator = context.coordinator
        coordinator.onPanned = onPanned
        coordinator.onLongPress = onLongPress
        coordinator.onRouteClick = onRouteClick
        mapView.showsUserLocation = map.showsUserLocation
        mapView.layoutMargins = UIEdgeInsets(top: 0, left: 0, bottom: bottomMargin, right: 0)
        if map.coveredArea != coordinator.coveredArea {
            coordinator.showCoveredArea(map.coveredArea, on: mapView)
        }
        coordinator.showRouteLines(routeLines, on: mapView)
        coordinator.showDestination(map.destination, on: mapView)

        // The map doesn't rotate with the heading: `.follow`, not `.followWithHeading`
        let mode: MKUserTrackingMode = map.followsUser && map.showsUserLocation ? .follow : .none
        if mode != mapView.userTrackingMode {
            coordinator.isChangingMode = true
            mapView.setUserTrackingMode(mode, animated: true)
            coordinator.isChangingMode = false
        }

        // A request moves the map to the overview when it isn't following; following, the tracking mode keeps it on the user.
        // While planning, the routes' frame takes over.
        if map.overviewRequest != coordinator.shownOverviewRequest {
            coordinator.shownOverviewRequest = map.overviewRequest
            if mode == .none, routeBounds == nil {
                mapView.setRegion(Self.region(for: map.overview), animated: true)
            }
        }

        // The routes are framed when their bounds change, and again when the sheet moves, once it has settled
        let boundsChanged = routeBounds != coordinator.routeBounds
        let sheetMoved = sheetTop != coordinator.sheetTop
        coordinator.routeBounds = routeBounds
        coordinator.sheetTop = sheetTop
        coordinator.isSheetLarge = isSheetLarge
        if boundsChanged || sheetMoved {
            coordinator.fitRoutesAfterSheetSettles(on: mapView)
        }
    }

    static func coordinate(_ point: DomainGeoPoint) -> CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: point.latitude, longitude: point.longitude)
    }

    private static func coordinates(_ points: [DomainGeoPoint]) -> [CLLocationCoordinate2D] {
        points.map(coordinate)
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

    final class Coordinator: NSObject, MKMapViewDelegate, UIGestureRecognizerDelegate {
        var onPanned: () -> Void
        var onLongPress: (DomainGeoPoint) -> Void
        var onRouteClick: (String) -> Void
        // Kept between presses, so the first tap is not late
        private let impact = UIImpactFeedbackGenerator(style: .medium)
        var isChangingMode = false
        var shownOverviewRequest: Int32 = 0
        var coveredArea: [DomainGeoPolygon] = []
        private var coveredOverlays: [MKOverlay] = []
        private var destination: (point: DomainGeoPoint, annotation: MKPointAnnotation)?
        var shownRouteLines: [RouteLine] = []
        var routeOverlays: [MKOverlay] = []
        var routeLabels: [RouteLabelAnnotation] = []
        var routeBounds: DomainGeoBounds?
        var pendingFit: DispatchWorkItem?
        var sheetTop: CGFloat = 0
        var isSheetLarge = false

        init(onPanned: @escaping () -> Void, onLongPress: @escaping (DomainGeoPoint) -> Void, onRouteClick: @escaping (String) -> Void) {
            self.onPanned = onPanned
            self.onLongPress = onLongPress
            self.onRouteClick = onRouteClick
        }

        func mapView(_ mapView: MKMapView, didChange mode: MKUserTrackingMode, animated: Bool) {
            // MapKit stops following when the user pans the map
            if mode == .none, !isChangingMode {
                onPanned()
            }
        }

        // MARK: Gestures

        @objc
        func handleLongPress(_ recognizer: UILongPressGestureRecognizer) {
            guard recognizer.state == .began, let mapView = recognizer.view as? MKMapView else {
                return
            }
            let coordinate = mapView.convert(recognizer.location(in: mapView), toCoordinateFrom: mapView)
            impact.impactOccurred()
            onLongPress(DomainGeoPoint(latitude: coordinate.latitude, longitude: coordinate.longitude))
        }

        func gestureRecognizer(
            _ gestureRecognizer: UIGestureRecognizer,
            shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer
        ) -> Bool {
            true
        }

        // MARK: Overlays

        func showCoveredArea(_ area: [DomainGeoPolygon], on mapView: MKMapView) {
            coveredArea = area
            mapView.removeOverlays(coveredOverlays)
            coveredOverlays = area.isEmpty ? [] : makeCoveredOverlays(area)
            mapView.addOverlays(coveredOverlays, level: .aboveRoads)
            // The routes stay above the dim, and this also re-adds them on a trait change
            mapView.removeOverlays(routeOverlays)
            mapView.addOverlays(routeOverlays, level: .aboveRoads)
        }

        // The chosen destination's pin, replaced only when the destination changes
        func showDestination(_ point: DomainGeoPoint?, on mapView: MKMapView) {
            if point == destination?.point {
                return
            }
            if let shown = destination {
                mapView.removeAnnotation(shown.annotation)
            }
            destination = point.map { point in
                let annotation = MKPointAnnotation()
                annotation.coordinate = CLLocationCoordinate2D(latitude: point.latitude, longitude: point.longitude)
                return (point, annotation)
            }
            if let shown = destination {
                mapView.addAnnotation(shown.annotation)
            }
        }

        // MARK: Renderers

        func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            switch overlay {
            case let polygon as MKPolygon:
                let renderer = MKPolygonRenderer(polygon: polygon)
                renderer.fillColor = MapView.dimColor.resolvedColor(with: mapView.traitCollection)
                return renderer
            case let route as RoutePolyline:
                return MapRoutes.renderer(for: route, traitCollection: mapView.traitCollection)
            case let polyline as MKPolyline:
                let renderer = MKPolylineRenderer(polyline: polyline)
                renderer.strokeColor = MapView.borderColor.resolvedColor(with: mapView.traitCollection)
                renderer.lineWidth = MapView.borderWidth
                return renderer
            default:
                return MKOverlayRenderer(overlay: overlay)
            }
        }

        // The world with the covered outlines cut out, then the holes and every outline and hole as a border
        private func makeCoveredOverlays(_ area: [DomainGeoPolygon]) -> [MKOverlay] {
            let world = MKMapRect.world
            let corners = [
                MKMapPoint(x: world.minX, y: world.minY),
                MKMapPoint(x: world.maxX, y: world.minY),
                MKMapPoint(x: world.maxX, y: world.maxY),
                MKMapPoint(x: world.minX, y: world.maxY)
            ]
            let outlines = area.map { MapView.coordinates($0.outline) }
            let holes = area.flatMap(\.holes).map { MapView.coordinates($0) }
            let interiorPolygons = outlines.map { MKPolygon(coordinates: $0, count: $0.count) }
            var overlays: [MKOverlay] = [MKPolygon(points: corners, count: corners.count, interiorPolygons: interiorPolygons)]
            overlays += holes.map { MKPolygon(coordinates: $0, count: $0.count) as MKOverlay }
            overlays += (outlines + holes).map { MKPolyline(coordinates: $0, count: $0.count) as MKOverlay }
            return overlays
        }
    }
}
