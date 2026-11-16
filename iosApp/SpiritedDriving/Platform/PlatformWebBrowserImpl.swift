import KMP
import SafariServices
import UIKit

final class PlatformWebBrowserImpl: PlatformWebBrowser {

    // Kotlin calls this from a background thread, while this class is on the main actor (the target's default isolation),
    // like `UIKit` must be. So it's nonisolated and moves to the main actor itself.
    nonisolated func open(url: String) {
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                // SFSafariViewController only opens web pages, and crashes on any other scheme.
                guard let url = URL(string: url), ["http", "https"].contains(url.scheme?.lowercased()) else {
                    return
                }
                guard let presenter = Self.topViewController() else {
                    return
                }
                presenter.present(SFSafariViewController(url: url), animated: true)
            }
        }
    }

    // Profile is a sheet over the map, so the page is presented from the sheet, which is the top of the presented stack.
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
