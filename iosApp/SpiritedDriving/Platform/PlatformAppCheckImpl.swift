import FirebaseAppCheck
import FirebaseCore
@preconcurrency import KMP

// Firebase App Check. Qualified as `FirebaseAppCheck.AppCheck`, as the KMP `AppCheck` class has the same name.
final class PlatformAppCheckImpl: PlatformAppCheck {

    // Kotlin calls this from a background thread, so it's nonisolated. The provider is set up in `AppDelegate`.
    nonisolated func token(listener: TokenListener) {
        guard FirebaseApp.app() != nil else {
            listener.onToken(token: nil)
            return
        }
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        FirebaseAppCheck.AppCheck.appCheck().token(forcingRefresh: false) { token, error in
            listener.onToken(token: error == nil ? token?.token : nil)
        }
    }
}
