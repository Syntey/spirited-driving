import GoogleSignIn
import SwiftUI

@main
struct SpiritedDrivingApp: App {

    @UIApplicationDelegateAdaptor var appDelegate: AppDelegate

    var body: some Scene {
        WindowGroup {
            RootView()
                .onOpenURL { _ = GIDSignIn.sharedInstance.handle($0) }
        }
    }
}
