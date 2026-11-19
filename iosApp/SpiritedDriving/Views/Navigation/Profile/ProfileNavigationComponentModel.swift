import KMP
import Observation

/// Observes the sheet slot of the Profile host, so the sign-in sheet over Profile is presented and dismissed natively.
@Observable
final class ProfileNavigationComponentModel {

    // MARK: Public computed properties

    var sheetItem: DecomposeSlotItem<ProfileSheetChild>? {
        get {
            guard let sheetChild = _sheet.child?.instance else {
                return nil
            }
            return DecomposeSlotItem(id: ObjectIdentifier(sheetChild), instance: sheetChild)
        }
        set { // swiftlint:disable:this unused_setter_value
            // Setting nil is a swipe down, which closes the sheet in Kotlin too
            guard _sheet.child?.instance != nil else {
                return
            }
            navHost.onSheetDismissed()
        }
    }

    // MARK: Private stored properties

    private var _sheet: ChildSlot<ProfileSheetConfig, ProfileSheetChild>
    private let navHost: ProfileNavHost

    // MARK: Private @ObservationIgnored properties

    @ObservationIgnored private var sheetTask: Task<Void, Never>?

    // MARK: Init / Deinit

    init(_ navHost: ProfileNavHost) {
        self.navHost = navHost
        _sheet = navHost.sheet.value

        // Safe: SKIE's StateFlow replays the current value to new collectors,
        // so no emissions are lost between the synchronous read above and this Task.
        sheetTask = Task { [weak self] in
            for await state in navHost.sheet {
                self?._sheet = state
            }
        }
    }

    deinit {
        sheetTask?.cancel()
    }
}
