import Foundation
import KMP

final class PlatformBindingsImpl: PlatformBindings {

    // Created here, on the main thread, as `CLLocationManager` must be
    private let locationPermissionImpl = PlatformLocationPermissionImpl()

    func platform() -> any Platform {
        PlatformImpl()
    }

    func firebaseCrashlytics() -> PlatformFirebaseCrashlytics {
        PlatformFirebaseCrashlyticsImpl()
    }

    func locationPermission() -> any PlatformLocationPermission {
        locationPermissionImpl
    }
}
