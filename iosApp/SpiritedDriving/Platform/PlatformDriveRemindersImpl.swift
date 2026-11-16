import KMP
import UIKit
import UserNotifications

final class PlatformDriveRemindersImpl: PlatformDriveReminders {

    // Kotlin calls these from a background thread. The notification centre calls back on its own queue, so the listener
    // is called on the main actor.
    nonisolated func isOn(listener: DriveRemindersListener) {
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            let isOn = settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional
            DispatchQueue.main.async {
                MainActor.assumeIsolated {
                    listener.onResult(isOn: isOn)
                }
            }
        }
    }

    // Provisional authorization shows no prompt, so it's asked for first when the user hasn't answered yet. Then the app's
    // notification settings, where the user can turn the notification on or off.
    nonisolated func openSettings() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            guard settings.authorizationStatus == .notDetermined else {
                Self.openAppNotificationSettings()
                return
            }
            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .provisional]) { _, _ in
                Self.openAppNotificationSettings()
            }
        }
    }

    private nonisolated static func openAppNotificationSettings() {
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                guard let url = URL(string: UIApplication.openNotificationSettingsURLString) else {
                    return
                }
                UIApplication.shared.open(url)
            }
        }
    }
}
