# iOS App — CLAUDE.md

iOS-specific conventions for the SwiftUI layer in `iosApp/`.

## Compose Multiplatform Screens

Screens are implemented in Compose Multiplatform (in their `:feature:*` module) and exposed to Swift as `*ScreenUiController`
functions. Their SwiftUI view is a thin `*Component` that takes the KMP screen directly — no ComponentModel,
as there is no state for Swift to observe:

```swift
struct SavedRoutesComponent: View {
    private let screen: SavedRoutesScreen

    init(_ screen: SavedRoutesScreen) {
        self.screen = screen
    }

    var body: some View {
        ComposeView { SavedRoutesScreenUiController(screen: screen) }
            .composeScreen()                                                    // Edge to edge, Compose applies safe area
            .navigationTitle(Localizable.saved_routes_screen_title.localized)  // Native navigation chrome stays in SwiftUI
    }
}
```

Native navigation (`NavigationStack`, sheets) and any native SwiftUI screen, such as the map, follow the
Component + ComponentModel pattern below.

## Component + ComponentModel Pattern

Navigation views and native SwiftUI screens follow the Component + ComponentModel pattern:

1. **Protocol** (`*ComponentModelProtocol`) — defines the public interface (properties, actions)
2. **ComponentModel** (`@Observable final class`) — conforms to the protocol, wraps KMP component
3. **Component** (`struct *Component<Model: *Protocol>: View`) — generic SwiftUI view

```swift
// Protocol
protocol MyComponentModelProtocol: AnyObject {
    var title: String { get }
    var alert: AlertModel? { get set }

    func onAction()
}

// ComponentModel
@Observable
final class MyComponentModel: MyComponentModelProtocol { ... }

// View
struct MyComponent<Model: MyComponentModelProtocol>: View {
    @State var model: Model
    ...
}
```

Screen-level protocols use `AnyObject` constraint. Navigation-level protocols do not.

### KMP Deviation Comment

Screen-level ComponentModel protocols include this doc comment:

```swift
/// KMP deviation: Conforms to `AnyObject` instead of FuturedKit's `ComponentModel` protocol
/// because navigation and event handling are managed by KMP Decompose, not a Swift Coordinator.
```

### Debug Mocks

Each screen ComponentModel has a `#if DEBUG` mock for SwiftUI previews:

```swift
#if DEBUG
@Observable
final class MyComponentModelMock: MyComponentModelProtocol {
    var title = "Preview Title"
    var alert: AlertModel?
    func onAction() {}
}
#endif
```

## Bindings to @State Model Properties

When creating a `Binding` to a property on an `@State` model, use the `$` projection syntax — never use verbose `Binding(get:set:)`:

```swift
// Correct
Picker("", selection: $model.selectedUnit) { ... }
.sheet(item: $model.sheetItem) { ... }
.alert("", isPresented: $model.alert.isPresented) { ... }

// Wrong
Picker("", selection: Binding(
    get: { model.selectedUnit },
    set: { model.onUnitSelected($0) }
)) { ... }
```

To make a property bindable, expose it with a getter/setter in the ComponentModel:

```swift
var selectedUnit: DistanceUnit {
    get { viewState.selectedUnit }
    set { actions.onUnitSelected(unit: newValue) }
}
```

## Slot Binding Dismiss Actions

Every Decompose slot binding used with `.sheet(item:)` or `.fullScreenCover(item:)` **must** call a KMP dismiss action in its setter — never leave it empty. Without this, SwiftUI swipe-to-dismiss does not notify KMP, leaving stale slot state.

```swift
// In ComponentModel
var sheetItem: DecomposeSlotItem<SomeSheetChild>? {
    get {
        guard let child = _sheet.child else { return nil }
        return DecomposeSlotItem(id: ObjectIdentifier(child), instance: child.instance)
    }
    set { // swiftlint:disable:this unused_setter_value
        actions.onSheetDismissed()
    }
}
```

## Navigation ComponentModel Structure

Navigation ComponentModels use MARK comments to organize sections:

```swift
@Observable
final class MyNavigationComponentModel: MyNavigationComponentModelProtocol {

    // MARK: Public computed properties
    ...

    // MARK: Private stored properties
    ...

    // MARK: Private @ObservationIgnored properties
    ...

    // MARK: Init / Deinit
    ...

    // MARK: Public functions
    ...
}
```

Views never access `model.actions` directly — all actions are wrapped in model methods or computed property setters.

## View Modifier Ordering

Screen-level views apply modifiers in this order:

```swift
var body: some View {
    content
        .background(...)                    // 1. Appearance
        .navigationTitle(...)               // 2. Navigation setup
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ... }
        .onChange(of: ...) { ... }          // 3. Event handling
}
```

Not every modifier is present on every screen — only include what the screen needs, but keep the relative order consistent.

For screens with complex content, extract a `contentView` computed property and apply modifiers to it in `body`.

## Localization

All user-facing strings **must** use KMP localization keys from `Localizable` — never hardcode text directly. The KMP module (`import KMP`) exposes `Localizable` with `.localized` for accessing translated strings from `strings.xml`.

```swift
// Correct
Text(Localizable.onboarding_what_title.localized)
Button(Localizable.onboarding_next.localized, action: model.onNext)

// Wrong — hardcoded string
Text("Take the fun way")
```

## Icons

Icons come from the KMP shared resources (`MR.images`, the Phosphor SVGs in `:core:resources`) — never add icons to
`Assets.xcassets`. The KMP module is the single source of truth for icons shared across platforms. Render a monochrome
icon as a template, so it takes the current foreground style:

```swift
// Correct — KMP icon with template rendering
MR.images().delete.image.renderingMode(.template)

// Wrong — adding icons to xcassets, or a system symbol
Image(systemName: "trash")
```

The exception is native chrome whose design names a system symbol: the map's glass buttons use SF Symbols
(`location`, `location.fill`, `location.slash`, `person.fill`), as in Apple Maps, since Phosphor has no arrow with a
slash.

## Struct Property Access Control

When a struct has a custom `init`, all stored properties that are not accessed from external call sites **must** be marked `private`. The custom `init` provides the public API — internal properties should not leak.

```swift
// Correct
struct MyCard<Content: View>: View {
    private let title: String
    @ViewBuilder private let content: () -> Content

    init(title: String, @ViewBuilder content: @escaping () -> Content) {
        self.title = title
        self.content = content
    }
}
```

## Tap Target Hit Areas

Empty whitespace inside a `Button` or tap gesture — from `Spacer()`, `.frame(maxWidth: .infinity)`, padding, etc. — is not tappable by default. Apply `.contentShape(.rect)` on the container to make the entire frame respond to taps:

```swift
Button(action: onTap) {
    HStack {
        Text("Label")
        Spacer()
    }
    .frame(maxWidth: .infinity)
    .contentShape(.rect)
}
```
