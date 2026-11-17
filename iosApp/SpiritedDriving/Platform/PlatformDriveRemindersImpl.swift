import Foundation
import KMP
import UIKit
import UserNotifications

final class PlatformDriveRemindersImpl: NSObject, PlatformDriveReminders, UNUserNotificationCenterDelegate {

    // One request per app, so a new drive's notification replaces the one scheduled before.
    nonisolated static let identifier = "drive_feedback"
    nonisolated static let driveIdKey = "driveId"

    // Read and written on the main actor only.
    private var openListener: (any DriveReminderOpenListener)?
    private var pendingDriveId: String?

    // Set here, as PlatformBindingsImpl is created in `application(_:didFinishLaunchingWithOptions:)`: a tap that starts the
    // app is delivered once the delegate is set, so it must be set before launch finishes.
    override init() {
        super.init()
        UNUserNotificationCenter.current().delegate = self
    }

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

    nonisolated func schedule(driveId: String, destinationName: String?, atEpochMillis: Int64) {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            guard settings.authorizationStatus == .notDetermined else {
                Self.add(driveId: driveId, destinationName: destinationName, atEpochMillis: atEpochMillis)
                return
            }
            UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .provisional]) { _, _ in
                Self.add(driveId: driveId, destinationName: destinationName, atEpochMillis: atEpochMillis)
            }
        }
    }

    // Removes only this drive's notification: the identifier is shared by every drive, so a notification of another drive
    // that replaced it is left as it is.
    nonisolated func cancel(driveId: String) {
        UNUserNotificationCenter.current().getPendingNotificationRequests { requests in
            let identifiers = requests
                .filter { Self.isNotification($0.identifier, $0.content.userInfo, of: driveId) }
                .map(\.identifier)
            UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: identifiers)
        }
        UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
            let identifiers = notifications
                .filter { Self.isNotification($0.request.identifier, $0.request.content.userInfo, of: driveId) }
                .map(\.request.identifier)
            UNUserNotificationCenter.current().removeDeliveredNotifications(withIdentifiers: identifiers)
        }
    }

    nonisolated func setOpenListener(listener: (any DriveReminderOpenListener)?) {
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                self.openListener = listener
                if let listener, let driveId = self.pendingDriveId {
                    self.pendingDriveId = nil
                    listener.onOpen(driveId: driveId)
                }
            }
        }
    }

    // Provisional authorization needs no offer, so there's never one to make.
    nonisolated func canOffer(listener: DriveRemindersListener) {
        listener.onResult(isOn: false)
    }

    // A tap comes on the notification centre's queue. The app opens its drive when it's in the foreground, and keeps the
    // tap until a listener is set when it isn't.
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let driveId = response.notification.request.content.userInfo[Self.driveIdKey] as? String
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let completionHandler = completionHandler
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                if let driveId {
                    self.deliverOpen(driveId: driveId)
                }
            }
            completionHandler()
        }
    }

    // Quiet: the notification never shows while the app is in front, which asks for the answer on its own.
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([])
    }

    private func deliverOpen(driveId: String) {
        if let openListener {
            openListener.onOpen(driveId: driveId)
        } else {
            pendingDriveId = driveId
        }
    }

    // Built on the main actor, as the strings are read there.
    private nonisolated static func add(driveId: String, destinationName: String?, atEpochMillis: Int64) {
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                let content = UNMutableNotificationContent()
                content.title = title(destinationName: destinationName)
                content.body = Localizable.drive_feedback_notification_body.localized
                content.interruptionLevel = .passive
                content.userInfo = [driveIdKey: driveId]
                let seconds = Date(timeIntervalSince1970: Double(atEpochMillis) / 1_000).timeIntervalSinceNow
                // A time interval trigger can't be in the past, so an overdue one fires at once.
                let trigger = UNTimeIntervalNotificationTrigger(timeInterval: max(1, seconds), repeats: false)
                let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
                UNUserNotificationCenter.current().add(request)
            }
        }
    }

    @MainActor
    private static func title(destinationName: String?) -> String {
        guard let destinationName else {
            return Localizable.drive_feedback_question_unnamed.localized
        }
        // The string's positional argument is written for Kotlin's formatting, so it's turned into the Objective-C one.
        let template = Localizable.drive_feedback_question.localized.replacingOccurrences(of: "%1$s", with: "%@")
        return String(format: template, destinationName)
    }

    private nonisolated static func isNotification(_ identifier: String, _ userInfo: [AnyHashable: Any], of driveId: String) -> Bool {
        identifier == Self.identifier && userInfo[driveIdKey] as? String == driveId
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
