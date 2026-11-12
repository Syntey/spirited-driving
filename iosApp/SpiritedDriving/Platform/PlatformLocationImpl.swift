import CoreLocation
import KMP

final class PlatformLocationImpl: NSObject, PlatformLocation, CLLocationManagerDelegate {

    private let manager = CLLocationManager()
    private var listener: LocationUpdateListener?

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = 5
    }

    // Kotlin calls this from a background thread, while this class is on the main actor, like `CLLocationManager` must be.
    // So it's nonisolated and moves to the main actor itself.
    nonisolated func startUpdates(listener: LocationUpdateListener) {
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                self.start(listener)
            }
        }
    }

    nonisolated func stopUpdates() {
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                self.manager.stopUpdatingLocation()
                self.listener = nil
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.last {
            send(location)
        }
    }

    // Without access, or no fix yet: there's nothing to send, and updates go on once there is
    func locationManager(_ manager: CLLocationManager, didFailWithError error: any Error) {}

    private func start(_ listener: LocationUpdateListener) {
        self.listener = listener
        if let location = manager.location {
            send(location)
        }
        manager.startUpdatingLocation()
    }

    private func send(_ location: CLLocation) {
        listener?.onLocation(fix: LocationFix(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        ))
    }
}
