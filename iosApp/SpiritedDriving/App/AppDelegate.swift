import FirebaseAppCheck
import FirebaseCore
import KMP
import SwiftUI

final class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication, // swiftlint:disable:next discouraged_optional_collection
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        configureFirebase()
        initializeSharedApplication()
        return true
    }

    // Firebase is off when GoogleService-Info.plist is missing, as on a fresh clone; see the copy phase in the Xcode project.
    private func configureFirebase() {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else {
            return
        }
        #if DEBUG
        FirebaseAppCheck.AppCheck.setAppCheckProviderFactory(AppCheckDebugProviderFactory())
        #endif
        // Release and Beta have no App Check provider yet: App Attest needs the Apple Developer Program licence.
        FirebaseApp.configure()
    }

    private func initializeSharedApplication() {
        let isDebugBuild: Bool

        #if DEBUG
        isDebugBuild = true
        #else
        isDebugBuild = false
        #endif

        KmpApplication().initializeSharedApplication(platformBindings: PlatformBindingsImpl(), isDebugBuild: isDebugBuild)
    }
}
