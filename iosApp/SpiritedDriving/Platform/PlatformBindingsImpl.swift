import Foundation
import KMP

final class PlatformBindingsImpl: PlatformBindings {

    // Created here, on the main thread, as `CLLocationManager` must be
    private let locationPermissionImpl = PlatformLocationPermissionImpl()

    // Created here, on the main thread, as `CLLocationManager` must be
    private let locationImpl = PlatformLocationImpl()

    private let driveRemindersImpl = PlatformDriveRemindersImpl()
    private let mapsAppImpl = PlatformMapsAppImpl()
    private let webBrowserImpl = PlatformWebBrowserImpl()

    func platform() -> any Platform {
        PlatformImpl()
    }

    func firebaseCrashlytics() -> PlatformFirebaseCrashlytics {
        PlatformFirebaseCrashlyticsImpl()
    }

    func locationPermission() -> any PlatformLocationPermission {
        locationPermissionImpl
    }

    func location() -> any PlatformLocation {
        locationImpl
    }

    func driveReminders() -> any PlatformDriveReminders {
        driveRemindersImpl
    }

    func mapsApp() -> any PlatformMapsApp {
        mapsAppImpl
    }

    func webBrowser() -> any PlatformWebBrowser {
        webBrowserImpl
    }
}
