import KMP
import MapKit
import UIKit

/// A route's label on the map, pinned at the route's midpoint: "1 · +27 min", or "Fastest · 38 min".
nonisolated final class RouteLabelAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D
    let routeId: String
    let number: Int?
    let text: String
    let isSelected: Bool

    init(coordinate: CLLocationCoordinate2D, routeId: String, number: Int?, text: String, isSelected: Bool) {
        self.coordinate = coordinate
        self.routeId = routeId
        self.number = number
        self.text = text
        self.isSelected = isSelected
    }
}

/// A route label as a pill in its route colour, with the text in white or dark, whichever contrasts with the colour.
/// The colours are resolved with the view's trait collection, so the pill follows light and dark mode.
final class RouteLabelView: MKAnnotationView {
    static let reuseIdentifier = "RouteLabel"
    private static let horizontalPadding: CGFloat = 8
    private static let verticalPadding: CGFloat = 2
    private static let cornerRadius: CGFloat = 6
    private static let borderWidth: CGFloat = 1.5
    private let textLabel = UILabel()

    override var annotation: MKAnnotation? {
        didSet { updateContent() }
    }

    override init(annotation: MKAnnotation?, reuseIdentifier: String?) {
        super.init(annotation: annotation, reuseIdentifier: reuseIdentifier)
        textLabel.font = UIFont.monospacedDigitSystemFont(ofSize: 13, weight: .semibold)
        addSubview(textLabel)
        layer.cornerRadius = Self.cornerRadius
        layer.borderWidth = Self.borderWidth
        // The label sits on the point, not above it
        centerOffset = .zero
        collisionMode = .none
        displayPriority = .required
        registerForTraitChanges([UITraitUserInterfaceStyle.self]) { (view: RouteLabelView, _) in
            view.updateColors()
        }
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not used")
    }

    private func updateContent() {
        guard let label = annotation as? RouteLabelAnnotation else {
            return
        }
        textLabel.text = label.text
        textLabel.sizeToFit()
        textLabel.frame.origin = CGPoint(x: Self.horizontalPadding, y: Self.verticalPadding)
        frame.size = CGSize(
            width: textLabel.frame.width + 2 * Self.horizontalPadding,
            height: textLabel.frame.height + 2 * Self.verticalPadding
        )
        zPriority = label.isSelected ? .max : .defaultUnselected
        isAccessibilityElement = true
        accessibilityLabel = label.text
        accessibilityTraits = .button
        updateColors()
    }

    private func updateColors() {
        guard let label = annotation as? RouteLabelAnnotation else {
            return
        }
        let route = MapRoutes.lineColor(for: label.number).resolvedColor(with: traitCollection)
        backgroundColor = route
        layer.borderColor = MapRoutes.routeCasingColor.resolvedColor(with: traitCollection).cgColor
        textLabel.textColor = MapRouteLabels.textColor(on: route)
    }
}

/// The route labels' annotations, and the route of the label a tap landed on.
enum MapRouteLabels {
    // The text is dark on the lighter route colours, the same rule as core/ui's RouteBadge
    private static let lightLuminance: CGFloat = 0.4
    // The light theme's onSurface, the text on the lighter route colours of the dark theme; no asset holds it
    // swiftlint:disable:next object_literal
    private static let onLightRoute = UIColor(red: 0x1A / 255, green: 0x1C / 255, blue: 0x1E / 255, alpha: 1)

    /// The text on a route's colour: dark on the lighter routes, white on the darker ones. [route] is resolved already.
    static func textColor(on route: UIColor) -> UIColor {
        relativeLuminance(route) > lightLuminance ? onLightRoute : .white
    }

    // One label per line, at its midpoint; a line drawn without a label, as the rating step's, has none
    static func labels(for lines: [RouteLine]) -> [RouteLabelAnnotation] {
        lines.compactMap { line in
            guard let label = line.label else {
                return nil
            }
            return RouteLabelAnnotation(
                coordinate: MapView.coordinate(line.labelPoint),
                routeId: line.id,
                number: line.number?.intValue,
                text: label.localized(),
                isSelected: line.isSelected
            )
        }
    }

    // The route whose label a tap landed on, if any
    static func routeId(at point: CGPoint, on mapView: MKMapView) -> String? {
        var view = mapView.hitTest(point, with: nil)
        while let current = view {
            if let label = current as? RouteLabelView {
                return (label.annotation as? RouteLabelAnnotation)?.routeId
            }
            view = current.superview
        }
        return nil
    }

    // WCAG relative luminance, as core/ui's Compose Color.luminance() computes it
    private static func relativeLuminance(_ color: UIColor) -> CGFloat {
        var red: CGFloat = 0
        var green: CGFloat = 0
        var blue: CGFloat = 0
        var alpha: CGFloat = 0
        color.getRed(&red, green: &green, blue: &blue, alpha: &alpha)
        func linear(_ component: CGFloat) -> CGFloat {
            component <= 0.03928 ? component / 12.92 : pow((component + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * linear(red) + 0.7152 * linear(green) + 0.0722 * linear(blue)
    }
}

extension MapView.Coordinator {
    // MARK: Annotation views

    // The start ring and the route labels have views of their own; the destination keeps MapKit's default pin
    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        switch annotation {
        case is StartAnnotation:
            let view = mapView.dequeueReusableAnnotationView(withIdentifier: StartPinView.reuseIdentifier)
                ?? StartPinView(annotation: annotation, reuseIdentifier: StartPinView.reuseIdentifier)
            view.annotation = annotation
            return view
        case is RouteLabelAnnotation:
            let view = mapView.dequeueReusableAnnotationView(withIdentifier: RouteLabelView.reuseIdentifier)
                as? RouteLabelView
                ?? RouteLabelView(annotation: annotation, reuseIdentifier: RouteLabelView.reuseIdentifier)
            view.annotation = annotation
            return view
        default:
            return nil
        }
    }
}
