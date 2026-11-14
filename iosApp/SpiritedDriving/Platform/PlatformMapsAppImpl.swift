import KMP
import UIKit

final class PlatformMapsAppImpl: PlatformMapsApp {

    nonisolated var kind: MapsAppKind {
        .appleMaps
    }

    // Kotlin calls this from a background thread, while this class is on the main actor (the target's default isolation),
    // like `UIApplication` must be. So it's nonisolated and moves to the main actor itself.
    nonisolated func open(link: String, listener: MapsAppListener) {
        // Kotlin objects can be used from any thread
        nonisolated(unsafe) let listener = listener
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                guard let url = URL(string: link) else {
                    assertionFailure("Invalid maps link: \(link)")
                    listener.onResult(result: .missing)
                    return
                }
                // Universal links only: with Apple Maps deleted, the call fails instead of opening the link in Safari.
                UIApplication.shared.open(url, options: [.universalLinksOnly: true]) { opened in
                    listener.onResult(result: opened ? .opened : .missing)
                }
            }
        }
    }

    nonisolated func openStore() {
        DispatchQueue.main.async {
            MainActor.assumeIsolated {
                guard let url = URL(string: "itms-apps://apps.apple.com/app/id915056765") else {
                    return
                }
                UIApplication.shared.open(url)
            }
        }
    }
}
