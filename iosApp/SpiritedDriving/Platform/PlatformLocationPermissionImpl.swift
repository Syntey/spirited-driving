import CoreLocation
import KMP
import UIKit

final class PlatformLocationPermissionImpl: NSObject, PlatformLocationPermission, CLLocationManagerDelegate {

    private let manager = CLLocationManager()
    private var pendingListeners: [LocationPermissionListener] = []

    override init() {
        super.init()
        manager.delegate = self
    }

    // Kotlin calls this from a background thread, while this class is on the main actor (the target's default isolation),
    // like `CLLocationManager` must be. So it's nonisolated and moves to the main actor itself.
    nonisolated func request(listener: LocationPermissionListener) {
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                self.ask(listener)
            }
        }
    }

    nonisolated func currentAccess(listener: LocationPermissionListener) {
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                listener.onResult(access: self.currentAccess())
            }
        }
    }

    nonisolated func openSettings() {
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                guard let url = URL(string: UIApplication.openSettingsURLString) else {
                    return
                }
                UIApplication.shared.open(url)
            }
        }
    }

    // Also called once the delegate is set, while the status is still undetermined.
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard manager.authorizationStatus != .notDetermined else {
            return
        }
        let listeners = pendingListeners
        pendingListeners = []
        listeners.forEach { $0.onResult(access: currentAccess()) }
    }

    private func ask(_ listener: LocationPermissionListener) {
        guard manager.authorizationStatus == .notDetermined else {
            listener.onResult(access: currentAccess())
            return
        }
        pendingListeners.append(listener)
        manager.requestWhenInUseAuthorization()
    }

    // Once the user has answered, iOS doesn't show the prompt again: a denial can only be changed in Settings.
    private func currentAccess() -> LocationAccess {
        switch manager.authorizationStatus {
        case .authorizedAlways, .authorizedWhenInUse:
            return manager.accuracyAuthorization == .fullAccuracy ? .precise : .approximate
        case .denied, .restricted:
            return .deniedForGood
        case .notDetermined:
            return .denied
        @unknown default:
            return .denied
        }
    }
}
