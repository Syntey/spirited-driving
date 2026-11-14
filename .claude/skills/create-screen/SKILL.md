---
name: create-screen
description: Add a screen (or sheet) to a feature - the Decompose component, view state, navigation contract, shared Compose UI with previews, and its wiring into the navigation hosts on Android and iOS. Also creates the feature module when it doesn't exist yet. Use whenever you add a screen, change how one is wired, or the user says "add the X screen", even without naming this skill.
---

# Create a screen

A screen is one package in a `:feature:*` module, plus a few lines of wiring in `:shared`, `androidApp` and `iosApp`.
Konsist checks the shape of components, view states and previews (`SCREEN-*`, `USECASE-1` in `konsist/`), and its
failure message says how to fix a violation.

## Before you start

- Read the screen's section in `docs/features/<feature>/design.md` and the feature's `README.md`. Which module owns a
  feature is in the "App structure" table in `docs/architecture.md`; which navigation host shows it, and whether it's
  a sheet, is in `docs/navigation.md`. Ask if the docs and the request disagree.
- Settle: the name `{Name}`, args (input from the screen that opens it), use cases, and whether it needs one-shot
  events. Events are only for platform effects, like showing a snackbar.
- No feature module yet? Create it first, see [New feature module](#new-feature-module).

## The screen package

Package `com.syntey.spiriteddriving.feature.<feature>.ui.<name>Screen` in `feature/<feature>/src/commonMain/kotlin/`, laid
out as in [Feature packages](#feature-packages).

**`{Name}Screen.kt`**: the public contract, implemented by the component, used by both platforms' UI.

```kotlin
interface {Name}Screen {
    val viewState: StateFlow<{Name}ViewState>
    val actions: Actions
    val events: Flow<{Name}UiEvent> // only with one-shot events

    interface Actions {
        fun onRouteSelected(routeId: String)
        fun onBack()

        companion object {
            fun noOpActions(): Actions = object : Actions {
                override fun onRouteSelected(routeId: String) = Unit
                override fun onBack() = Unit
            }
        }
    }
}
```

Actions say what the user did (`onRouteSelected`), not what happens next (`openMaps`).

**`{Name}ViewState.kt`**

```kotlin
data class {Name}ViewState(
    val title: StringDesc = "".desc(),
    val routes: ImmutableList<RouteItem> = persistentListOf(),
)
```

- Every field has a default, since the component starts from `{Name}ViewState()` and previews set only the fields
  they show. Text is `StringDesc`, which each platform's UI resolves, so the component needs no platform context.
  Lists are `ImmutableList`, derived values are `get()` properties. A screen without state uses
  `data object {Name}ViewState`.
- Loading and error state isn't standardized yet; the first real screen sets the shape and updates this section.
  Until then, a failure of something the user did must show in the state, never end in an empty `onError`.

**`{Name}ScreenNavigation.kt`**: public, since the navigator in `:shared` implements it.

```kotlin
interface {Name}ScreenNavigation : NavigationActions {
    fun {Name}Screen.pop()
    fun {Name}Screen.openRouteDetail(routeId: String)
}
```

**`{Name}Args.kt`**: only when the screen takes input. Args are saved with the navigation state, so pass IDs and small
values, not lists or domain objects.

```kotlin
@Serializable
data class {Name}Args(val tripId: String)
```

**`{Name}UiEvent.kt`**: only with one-shot events. `sealed interface {Name}UiEvent : UiEvent`, sent with
`sendUiEvent(...)` and handled in the UI with `EventsEffect`.

**`{Name}Component.kt`**

```kotlin
@Factory
@GenerateFactory
internal class {Name}Component(
    @InjectedParam componentContext: AppComponentContext,
    @InjectedParam override val navigation: {Name}ScreenNavigation,
    @InjectedParam private val args: {Name}Args, // only with args, always after navigation
    private val getRoutesUseCase: GetRoutesUseCase,
) : ScreenComponent<{Name}ViewState, Nothing, {Name}ScreenNavigation>( // {Name}UiEvent instead of Nothing with events
    componentContext = componentContext,
    defaultState = {Name}ViewState(),
),
    {Name}Screen,
    {Name}ScreenNavigation by navigation,
    {Name}Screen.Actions {

    override val viewState: StateFlow<{Name}ViewState> = componentState
    override val actions: {Name}Screen.Actions = this

    init {
        doOnCreate { loadRoutes() }
    }

    override fun onRouteSelected(routeId: String) = openRouteDetail(routeId)
    override fun onBack() = pop()

    private fun loadRoutes() = getRoutesUseCase.execute(GetRoutesUseCase.Args(args.tripId)) {
        onSuccess { routes -> componentState.update { it.copy(routes = routes.toItems()) } }
        onError { error -> /* put a user-visible error into the state */ }
    }
}
```

- `init` only lists named calls, and action overrides are one-liners (a navigation call or a private function), so
  the component reads as what happens on start and on each action. State changes only through
  `componentState.update { it.copy(...) }`.
- Logic beyond mapping to the view state goes into a use case in the feature's `domain` package, implementing
  Arkitekt's `UseCase<Args, T>` or `FlowUseCase<Args, T>`.
- A `FlowUseCase` that should run only while the screen is visible (location updates, sensors) runs with
  `executeWithLifecycle` from `core/ui/base` instead of `execute`: it pauses on stop and resumes on start.

**`{Name}ScreenUi.kt`**: shared Compose for both platforms.

```kotlin
@Composable
fun {Name}ScreenUi(screen: {Name}Screen, modifier: Modifier = Modifier) {
    val viewState by screen.viewState.collectAsStateWithLifecycle()
    Content(viewState = viewState, actions = screen.actions, modifier = modifier)
}

@Composable
private fun Content(viewState: {Name}ViewState, actions: {Name}Screen.Actions, modifier: Modifier = Modifier) {
    // ...
}

@ScreenPreviews
@Composable
private fun {Name}ScreenPreview() = Showcase {
    Content(viewState = {Name}PreviewStates.viewState(), actions = noOpActions())
}
```

- The screen draws only its content: no top bar, back button or system insets. The navigation hosts draw those. A
  screen that draws behind the system bars, like First launch with its full-screen illustration, keeps its own
  controls clear of them with `WindowInsets.safeDrawing`.
- Colors, type and shapes come from the theme (`MaterialTheme.*`, `AppTheme.*` for what Material has no slot for),
  never literal values. Icons are `painterResource(MR.images.…)` from `:core:resources`, and `:core:ui` components take
  their texts and icons as parameters. A value from a design's Platforms table comes from `AppTheme.platformStyle`, which holds
  both platforms' values in `PlatformStyle` in `:core:ui`; add it there, never branch on the platform for styling in
  the screen ("Shared and native UI" in `docs/architecture.md`). A component only this feature uses lives in the
  feature, and its values go in the feature's `{Feature}PlatformStyle` (see `MapPlatformStyle`), not in `PlatformStyle`.
  Copy that names a platform's app picks its string by `AppTheme.platform`.
- Strings go into `core/resources/src/commonMain/moko-resources/base/strings.xml`, then
  `./gradlew generateMRcommonMain`.
- One preview per state that looks different (loaded, empty, error). Use the multipreview annotations from
  `core.ui.preview`, not a bare `@Preview`: `@ScreenPreviews` on a screen's content, `@ComponentPreviews` on a smaller
  composable, like one in `:core:ui`. A preview in `Showcase(platform = UiPlatform.Ios)` shows the iOS look in Android
  Studio.

**`model/ui/{Name}PreviewStates.kt`**: sample view states that several previews share (see `TripPlanPreviewStates`).
A preview's own data stays in its file.

```kotlin
internal object {Name}PreviewStates {
    fun viewState(routes: ImmutableList<RouteItem> = ...) = {Name}ViewState(routes = routes)
}
```

### Feature packages

A feature is split by layer, not by screen. Under `com.syntey.spiriteddriving.feature.<feature>`:

```
domain/            use cases
injection/         the Koin module
model/ui/          types view states are built from (row models, sub-states), and {Name}PreviewStates
tools/             pure helpers, such as text formatting, and Constants.kt
ui/                <Feature>Screens, and ViewControllers.ios.kt in iosMain
ui/components/     composables beyond a screen's entry one, shared by the feature's screens
ui/<name>Screen/   the contract, component, view state, navigation, UI events and entry composable
```

- `androidMain` and `commonTest` mirror these packages: `MapScreenUi.android.kt` is in `ui/tripPlanScreen`, a test of
  `model/ui/SearchState` in `model/ui`. `iosMain/ui/ViewControllers.ios.kt` sits next to `<Feature>Screens`.
- Previews go at the bottom of the file of the composable they show, never in a separate file. Only sample view states
  shared by several previews get their own file, `model/ui/{Name}PreviewStates.kt`.

## Wiring

1. **Feature entry point**: a function in the feature's `<Feature>Screens` object. Components are internal, so this
   is the only way `:shared` creates them.

   ```kotlin
   fun {name}(componentContext: AppComponentContext, navigation: {Name}ScreenNavigation, args: {Name}Args): {Name}Screen =
       {Name}ComponentFactory.createComponent(componentContext, navigation, args)
   ```

2. **iOS controller** in `feature/<feature>/src/iosMain/.../ViewControllers.ios.kt` (`fullScreenViewController` instead
   of `screenViewController` for a screen that draws behind the system bars):

   ```kotlin
   fun {Name}ScreenUiController(screen: {Name}Screen): UIViewController = screenViewController {
       {Name}ScreenUi(screen = screen, modifier = Modifier.fillMaxSize())
   }
   ```

3. **Navigation host** in `shared/src/commonMain/kotlin/.../shared/navigation/<host>/`:
   - `<Host>NavHost.kt`: a config (`@Serializable data object {Name}`, or `data class {Name}(val args: {Name}Args)`)
     and a child (`data class {Name}(val screen: {Name}Screen)`). A sheet goes into `<Host>SheetConfig` /
     `<Host>SheetChild` instead.
   - `<Host>NavHostNavigation.kt`: add `{Name}ScreenNavigation` to the interface's supertypes, and implement its
     functions in the navigator (`stackNavigator.pushNew(...)`, `stackNavigator.pop()`, `sheetNavigator.activate(...)`).
   - `<Host>NavHostComponent.kt`: a branch in `childFactory` that calls `<Feature>Screens.{name}(...)`.
4. **Android**: in `androidApp/.../ui/navigation/<Host>NavHostUi.kt`, render the child with `{Name}ScreenUi` and
   add its title to the `title` mapping (a sheet gets a `ModalBottomSheet` branch, with
   `dragHandle = { DragHandle() }` from `:core:ui`, not Material's default).
5. **iOS**:
   - `iosApp/SpiritedDriving/Views/Scenes/{Name}/{Name}Component.swift`:

     ```swift
     struct {Name}Component: View {
         private let screen: {Name}Screen

         init(_ screen: {Name}Screen) {
             self.screen = screen
         }

         var body: some View {
             ComposeView { {Name}ScreenUiController(screen: screen) }
                 .composeScreen()
                 .navigationTitle(Localizable.{name}_screen_title.localized)
         }
     }
     ```

   - Add `case let .{name}(entry): {Name}Component(entry.screen)` to the switch in the host's
     `iosApp/SpiritedDriving/Views/Navigation/<Folder>/<Folder>NavigationComponent.swift`, where `<Folder>` is the
     host's folder, such as `Root` (a sheet goes into the `.sheet` switch). Xcode picks up new files by itself.

## New feature module

1. `settings.gradle.kts`: `include(":feature:<feature>")`.
2. `feature/<feature>/build.gradle.kts`: `plugins { id(libs.plugins.conventions.kmp.feature.get().pluginId) }`. The
   package and namespace come from the path. Add `implementation(projects.core.platform)` only when the feature uses
   a platform binding.
3. `injection/<Feature>Module.kt`: `@Module`, `@ComponentScan("com.syntey.spiriteddriving.feature.<feature>")`,
   `@Configuration`, like `OnboardingModule`.
4. `ui/<Feature>Screens.kt` (an `object`, like `MapScreens`) and `src/iosMain/.../ui/ViewControllers.ios.kt` (with the
   file-level `@Suppress` from onboarding's).
5. `shared/build.gradle.kts`: `implementation(projects.feature.<feature>)` in `commonMain`, `api(...)` in `iosMain`,
   and `export(...)` in the framework block. `androidApp/build.gradle.kts`: `implementation(projects.feature.<feature>)`,
   as its navigation hosts draw the screens.

## Before you call it done

Besides the Definition of done in `CLAUDE.md`, check that the previews show every state in the screen's design.
