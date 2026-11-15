import KMP
import SwiftUI

struct ProfileNavigationComponent: View {
    private let navHost: ProfileNavHost

    init(navHost: ProfileNavHost) {
        self.navHost = navHost
    }

    var body: some View {
        DecomposeNavigationStack(
            kotlinStack: navHost.stack,
            setPath: { navHost.iosPopTo(newStack: $0) },
            content: { child in
                switch onEnum(of: child) {
                case let .profile(entry):
                    ProfileComponent(entry.screen)
                        .toolbar {
                            ToolbarItem(placement: .topBarTrailing) {
                                Button(role: .close) {
                                    navHost.onClose()
                                }
                            }
                        }
                case let .settings(entry):
                    SettingsComponent(entry.screen)
                case let .feedback(entry):
                    FeedbackComponent(entry.screen)
                case let .about(entry):
                    AboutComponent(entry.screen)
                case let .account(entry):
                    AccountComponent(entry.screen)
                case let .licences(entry):
                    LicencesComponent(entry.screen)
                case let .licenceText(entry):
                    LicenceTextComponent(entry.screen)
                }
            }
        )
    }
}
