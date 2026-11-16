import KMP
import Observation

protocol RootNavigationComponentModelProtocol {
    var slotChild: RootChild? { get }
    /// The Profile sheet's navigation host while Profile is open; nil otherwise.
    var profileNavHost: ProfileNavHost? { get }
    /// The Profile sheet as a slot item; setting nil (a swipe down) closes Profile in Kotlin.
    var profileSheetItem: DecomposeSlotItem<ProfileNavHost>? { get set }
}

@Observable
final class RootNavigationComponentModel: RootNavigationComponentModelProtocol {

    // MARK: Public computed properties

    var slotChild: RootChild? { _slot.child?.instance }

    var profileNavHost: ProfileNavHost? { profileSheetItem?.instance }

    var profileSheetItem: DecomposeSlotItem<ProfileNavHost>? {
        get {
            guard let sheetChild = _sheet.child?.instance else {
                return nil
            }
            switch onEnum(of: sheetChild) {
            case let .profile(entry):
                return DecomposeSlotItem(id: ObjectIdentifier(entry), instance: entry.navHost)
            }
        }
        set { // swiftlint:disable:this unused_setter_value
            profileNavHost?.onClose()
        }
    }

    // MARK: Private stored properties

    private var _slot: ChildSlot<RootConfig, RootChild>
    private var _sheet: ChildSlot<RootSheetConfig, RootSheetChild>

    // MARK: Private @ObservationIgnored properties

    @ObservationIgnored private var stateTask: Task<Void, Never>?
    @ObservationIgnored private var sheetTask: Task<Void, Never>?

    // MARK: Init / Deinit

    init(_ component: RootNavHost) {
        _slot = component.slot.value
        _sheet = component.sheet.value

        // Safe: SKIE's StateFlow replays the current value to new collectors,
        // so no emissions are lost between the synchronous read above and this Task.
        stateTask = Task { [weak self] in
            for await state in component.slot {
                self?._slot = state
            }
        }
        sheetTask = Task { [weak self] in
            for await state in component.sheet {
                self?._sheet = state
            }
        }
    }

    deinit {
        stateTask?.cancel()
        sheetTask?.cancel()
    }
}
