import GoogleSignIn
import SwiftUI

@main
struct KmpTemplateApp: App {

    @UIApplicationDelegateAdaptor var appDelegate: AppDelegate

    var body: some Scene {
        WindowGroup {
            RootView()
                .onOpenURL { _ = GIDSignIn.sharedInstance.handle($0) }
        }
    }
}
