import Dispatch
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
@preconcurrency import KMP
import UIKit

// Firebase Auth and Google sign-in. Sign in with Apple isn't available until the Apple Developer Program licence is.
// The KMP `Auth` class and FirebaseAuth's `Auth` clash, so Firebase's is always qualified.
final class PlatformAuthImpl: PlatformAuth {

    // Kotlin calls these from a background thread, so they're nonisolated. Firebase Auth is thread-safe; the Google
    // sign-in sheet is UIKit, so it moves to the main actor itself.

    nonisolated func currentUser() -> AuthUser? {
        guard isFirebaseConfigured() else {
            return nil
        }
        return FirebaseAuth.Auth.auth().currentUser.map(authUser(from:))
    }

    nonisolated func addUserListener(listener: AuthUserListener) -> AuthListenerRegistration {
        guard isFirebaseConfigured() else {
            listener.onUser(user: nil)
            return PlatformAuthListenerRegistration(handle: nil)
        }
        // Firebase calls the listener at once with the current user.
        let handle = FirebaseAuth.Auth.auth().addStateDidChangeListener { _, user in
            listener.onUser(user: user.map(authUser(from:)))
        }
        return PlatformAuthListenerRegistration(handle: handle)
    }

    nonisolated func signIn(provider: SignInProvider, listener: SignInListener) {
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                guard provider == .google else {
                    listener.onFailed(message: "Sign in with Apple isn't available yet")
                    return
                }
                self.signInWithGoogle(listener)
            }
        }
    }

    nonisolated func signOut() {
        guard isFirebaseConfigured() else {
            return
        }
        try? FirebaseAuth.Auth.auth().signOut()
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                GIDSignIn.sharedInstance.signOut()
            }
        }
    }

    nonisolated func idToken(listener: TokenListener) {
        guard isFirebaseConfigured(), let user = FirebaseAuth.Auth.auth().currentUser else {
            listener.onToken(token: nil)
            return
        }
        user.getIDTokenForcingRefresh(false) { token, error in
            listener.onToken(token: error == nil ? token : nil)
        }
    }

    // The sheet is shown from the top view controller: Profile is a sheet over the map, so it's the top of the presented stack.
    private func signInWithGoogle(_ listener: SignInListener) {
        // Kotlin objects can be used from any thread, and the sign-in callbacks come back on the main thread.
        nonisolated(unsafe) let listener = listener
        guard isFirebaseConfigured(), let clientID = FirebaseApp.app()?.options.clientID else {
            listener.onFailed(message: "Firebase isn't configured")
            return
        }
        guard let presenter = Self.topViewController() else {
            listener.onFailed(message: "Sign-in isn't available right now")
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error {
                report(error, to: listener)
                return
            }
            guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                listener.onFailed(message: "Sign-in failed")
                return
            }
            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: user.accessToken.tokenString
            )
            FirebaseAuth.Auth.auth().signIn(with: credential) { _, error in
                if let error {
                    report(error, to: listener)
                } else {
                    listener.onSignedIn()
                }
            }
        }
    }

    private static func topViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        var top = scene?.windows.first(where: \.isKeyWindow)?.rootViewController
        while let presented = top?.presentedViewController {
            top = presented
        }
        return top
    }
}

/// Firebase isn't configured without GoogleService-Info.plist in the app bundle, see `AppDelegate`.
nonisolated private func isFirebaseConfigured() -> Bool {
    FirebaseApp.app() != nil
}

nonisolated private func authUser(from user: FirebaseAuth.User) -> AuthUser {
    AuthUser(
        id: user.uid,
        name: nilIfBlank(user.displayName),
        email: nilIfBlank(user.email),
        provider: user.providerData.contains { $0.providerID == "apple.com" } ? .apple : .google
    )
}

/// Maps a sign-in error to the matching callback. Exactly one of the listener's methods is called.
nonisolated private func report(_ error: Error, to listener: SignInListener) {
    let nsError = error as NSError
    if nsError.domain == kGIDSignInErrorDomain, nsError.code == GIDSignInError.canceled.rawValue {
        listener.onCancelled()
    } else if nsError.domain == AuthErrorDomain, nsError.code == AuthErrorCode.accountExistsWithDifferentCredential.rawValue {
        listener.onAccountExistsWithOtherProvider(provider: .apple)
    } else {
        listener.onFailed(message: error.localizedDescription)
    }
}

/// Blank strings count as not shared, as they do on Android.
nonisolated private func nilIfBlank(_ value: String?) -> String? {
    guard let value, !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
        return nil
    }
    return value
}

// Removes the Firebase listener. Kotlin may remove it from any thread.
nonisolated final class PlatformAuthListenerRegistration: AuthListenerRegistration {

    private let handle: NSObjectProtocol?

    init(handle: NSObjectProtocol?) {
        self.handle = handle
    }

    func remove() {
        guard let handle else {
            return
        }
        FirebaseAuth.Auth.auth().removeStateDidChangeListener(handle)
    }
}
