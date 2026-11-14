import KMP
import MapKit
import UIKit

/// A route's line or casing, carrying the route it belongs to, so the renderer can style it.
nonisolated final class RoutePolyline: MKPolyline {
    var routeId = ""
    var number: Int?
    var isSelected = false
    var isCasing = false
}

/// The routes' lines on the map: their overlays, how they're drawn, and the frame that fits them above the sheet.
enum MapRoutes {
    // The route colours and casing from core/ui's Colors.kt, as the assets mirror them
    private static let routeFastestColor = UIColor(resource: .routeFastest)
    private static let routeFun1Color = UIColor(resource: .routeFun1)
    private static let routeFun2Color = UIColor(resource: .routeFun2)
    private static let routeFun3Color = UIColor(resource: .routeFun3)
    static let routeCasingColor = UIColor(resource: .routeCasing)
    private static let selectedLineWidth: CGFloat = 6
    private static let selectedCasingWidth: CGFloat = 9
    private static let unselectedLineWidth: CGFloat = 4
    private static let unselectedCasingWidth: CGFloat = 6
    private static let unselectedAlpha: CGFloat = 0.5
    // How far in points a tap can be from a route line and still pick it
    private static let tapRadius: CGFloat = 22

    // The frame's padding from the safe area's top, the sides and the sheet's top: wide enough that the pins at the
    // routes' ends stay on screen
    static let fitPadding: CGFloat = 48
    // How long the sheet has to stay put before the frame follows it, so a drag or the detent animation doesn't move the map
    // on every step
    static let sheetSettleDelay: TimeInterval = 0.3

    // Each route's casing, then its line over it
    static func overlays(for lines: [RouteLine]) -> [MKOverlay] {
        lines.flatMap { line -> [MKOverlay] in
            let coordinates = line.points.map(MapView.coordinate)
            return [true, false].map { isCasing in
                let route = RoutePolyline(coordinates: coordinates, count: coordinates.count)
                route.routeId = line.id
                route.number = line.number?.intValue
                route.isSelected = line.isSelected
                route.isCasing = isCasing
                return route
            }
        }
    }

    static func renderer(for route: RoutePolyline, traitCollection: UITraitCollection) -> MKPolylineRenderer {
        let renderer = MKPolylineRenderer(polyline: route)
        renderer.lineCap = .round
        renderer.lineJoin = .round
        let baseColor = route.isCasing ? routeCasingColor : lineColor(for: route.number)
        renderer.strokeColor = baseColor
            .resolvedColor(with: traitCollection)
            .withAlphaComponent(route.isSelected ? 1 : unselectedAlpha)
        renderer.lineWidth = route.isCasing
            ? (route.isSelected ? selectedCasingWidth : unselectedCasingWidth)
            : (route.isSelected ? selectedLineWidth : unselectedLineWidth)
        return renderer
    }

    // The route whose line is nearest the tap, within reach. The selected one is checked first, so it wins a tie.
    static func routeId(near point: CGPoint, in lines: [RouteLine], on mapView: MKMapView) -> String? {
        let ordered = lines.filter(\.isSelected) + lines.filter { !$0.isSelected }
        var nearest: (id: String, gap: CGFloat)?
        for line in ordered {
            let screenPoints = line.points.map { mapView.convert(MapView.coordinate($0), toPointTo: mapView) }
            let gap = zip(screenPoints, screenPoints.dropFirst())
                .map { segmentDistance(from: point, toSegment: $0, $1) }
                .min() ?? .infinity
            if gap <= tapRadius, gap < (nearest?.gap ?? .infinity) {
                nearest = (line.id, gap)
            }
        }
        return nearest?.id
    }

    static func mapRect(for bounds: DomainGeoBounds) -> MKMapRect {
        let southWest = MKMapPoint(MapView.coordinate(bounds.southWest))
        let northEast = MKMapPoint(MapView.coordinate(bounds.northEast))
        return MKMapRect(
            x: min(southWest.x, northEast.x),
            y: min(southWest.y, northEast.y),
            width: abs(southWest.x - northEast.x),
            height: abs(southWest.y - northEast.y)
        )
    }

    static func lineColor(for number: Int?) -> UIColor {
        switch number {
        case nil:
            routeFastestColor
        case 1:
            routeFun1Color
        case 2:
            routeFun2Color
        default:
            routeFun3Color
        }
    }

    // The distance from a point to a segment: to its nearest point, clamped to its ends
    private static func segmentDistance(from point: CGPoint, toSegment start: CGPoint, _ end: CGPoint) -> CGFloat {
        let deltaX = end.x - start.x
        let deltaY = end.y - start.y
        let lengthSquared = deltaX * deltaX + deltaY * deltaY
        var progress: CGFloat = 0
        if lengthSquared > 0 {
            progress = min(max(((point.x - start.x) * deltaX + (point.y - start.y) * deltaY) / lengthSquared, 0), 1)
        }
        return hypot(point.x - (start.x + progress * deltaX), point.y - (start.y + progress * deltaY))
    }
}

extension MapView.Coordinator {
    // The routes' lines, rebuilt only when they change; the selected one is last, so it's drawn on top
    func showRouteLines(_ lines: [RouteLine], on mapView: MKMapView) {
        guard lines != shownRouteLines else {
            return
        }
        shownRouteLines = lines
        mapView.removeOverlays(routeOverlays)
        mapView.removeAnnotations(routeLabels)
        routeOverlays = MapRoutes.overlays(for: lines)
        routeLabels = MapRouteLabels.labels(for: lines)
        mapView.addOverlays(routeOverlays, level: .aboveRoads)
        mapView.addAnnotations(routeLabels)
    }

    @objc
    func handleTap(_ recognizer: UITapGestureRecognizer) {
        guard recognizer.state == .ended, let mapView = recognizer.view as? MKMapView else {
            return
        }
        let point = recognizer.location(in: mapView)
        // A label picks its own route, even where another route's line runs under it
        if let routeId = MapRouteLabels.routeId(at: point, on: mapView)
            ?? MapRoutes.routeId(near: point, in: shownRouteLines, on: mapView) {
            onRouteClick(routeId)
        }
    }

    // Frames the routes above the sheet; not at full height, where the sheet covers the map
    func fitRoutes(on mapView: MKMapView) {
        guard let routeBounds, !isSheetLarge, sheetTop > 0 else {
            return
        }
        let padding = MapRoutes.fitPadding
        // The map fills the screen, so the sheet covers it from its top edge down
        let covered = max(mapView.bounds.height - sheetTop, 0)
        // MapKit fits inside the layout margins (the safe area and the collapsed sheet) and the padding on top of them, so
        // the padding is what's left after the margins
        let margins = mapView.layoutMargins
        let insets = UIEdgeInsets(
            top: max(mapView.safeAreaInsets.top + padding - margins.top, 0),
            left: max(padding - margins.left, 0),
            bottom: max(covered + padding - margins.bottom, 0),
            right: max(padding - margins.right, 0)
        )
        mapView.setVisibleMapRect(MapRoutes.mapRect(for: routeBounds), edgePadding: insets, animated: true)
    }

    // Frames the routes once the sheet has stayed put for a moment; each new call starts the wait again
    func fitRoutesAfterSheetSettles(on mapView: MKMapView) {
        pendingFit?.cancel()
        let fit = DispatchWorkItem { [weak self, weak mapView] in
            guard let self, let mapView else {
                return
            }
            fitRoutes(on: mapView)
        }
        pendingFit = fit
        DispatchQueue.main.asyncAfter(deadline: .now() + MapRoutes.sheetSettleDelay, execute: fit)
    }
}
