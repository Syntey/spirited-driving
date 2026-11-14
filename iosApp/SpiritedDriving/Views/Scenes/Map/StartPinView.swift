import KMP
import MapKit
import UIKit

/// The pin of a start picked by hand, drawn as a ring so it reads apart from the destination's pin and the user's location dot.
nonisolated final class StartAnnotation: NSObject, MKAnnotation {
    let coordinate: CLLocationCoordinate2D

    init(coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
    }
}

/// A 16 pt ring with a 3 pt border, centred on its point: surface fill, on-surface border.
/// The colours are resolved with the view's trait collection, so the ring follows light and dark mode.
final class StartPinView: MKAnnotationView {
    static let reuseIdentifier = "StartPin"
    private static let diameter: CGFloat = 16
    private static let borderWidth: CGFloat = 3

    override init(annotation: MKAnnotation?, reuseIdentifier: String?) {
        super.init(annotation: annotation, reuseIdentifier: reuseIdentifier)
        frame = CGRect(x: 0, y: 0, width: Self.diameter, height: Self.diameter)
        layer.cornerRadius = Self.diameter / 2
        layer.borderWidth = Self.borderWidth
        // The ring sits on the point, not above it
        centerOffset = .zero
        collisionMode = .none
        displayPriority = .required
        canShowCallout = false
        zPriority = .min
        registerForTraitChanges([UITraitUserInterfaceStyle.self]) { (view: StartPinView, _) in
            view.updateColors()
        }
        updateColors()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not used")
    }

    private func updateColors() {
        backgroundColor = .systemBackground
        layer.borderColor = UIColor.label.resolvedColor(with: traitCollection).cgColor
    }
}
