# Spirited Driving

Kotlin Multiplatform app for Android and iOS, with shared Compose screens. Design starts at `docs/overview.md`; how the
app is built is in `docs/architecture.md`.

## Commands
- `./gradlew lintCheck` - ktlint, detekt, Android lint and the Konsist architecture rules (`konsist/`); any error fails it
- `./gradlew ktlintFormat` - Fix formatting
- `./gradlew testAndroidHostTest` - Every shared module's tests on the JVM, as CI runs them; `:<module>:testAndroidHostTest` for one
- `./gradlew :konsist:test` - Only the architecture rules
- `./gradlew generateMRcommonMain` - Regenerate resource accessors after editing `core/resources/.../strings.xml`
- `./gradlew :androidApp:assembleDebug` - Build the Android app
- iOS app: `./gradlew assembleAndCopyDebugSwiftPackage -PkmpFrameworkTargets=simulator -PisStatic=false`, then
  `xcodebuild -project iosApp/SpiritedDriving.xcodeproj -scheme SpiritedDriving -destination 'generic/platform=iOS Simulator' ARCHS=arm64 -quiet build`.
  Xcode doesn't rebuild the Kotlin framework itself, and `-PisStatic=false` matches Debug builds, which SwiftUI previews need.

## Definition of done
`lintCheck` and `testAndroidHostTest` pass, both apps build, and business logic and data mapping have tests.

## Skills
- `create-screen` - A screen or sheet, or a new feature module
- `network-worker` - A backend API call: domain interface, DTOs, Ktorfit API, mapping and errors
- `platform-binding` - A native SDK or device service behind a `:core:platform` interface

Load the matching skill before writing code in its area, even if it didn't load by itself.

Rules for Kotlin, tests and Konsist load from `.claude/rules/` when you touch matching files. If a
skill or rule is wrong or out of date, fix it in the same change.

## Conventions
- **Modules**: `:feature:*` and `:data:*` depend on `:core:*` only (the build enforces it); features reach data
  through `:core:domain` interfaces, and navigation hosts and Koin wiring live in `:shared`. See README "Modules".
- **Layers**: Decompose components hold immutable view state, handle actions and drive navigation; use cases hold the
  logic; `:data:*` maps DTOs and network errors to domain types.
- **Naming**: `*Component`, `*Screen`, `*ViewState`, `*ScreenNavigation`, `*UseCase`, `*Dto`, `*Api`, `Api*` for
  implementations of domain interfaces.

## Agent skills

### Issue tracker

Issues are local markdown files under `.scratch/<feature>/`, which git ignores. See `docs/agents/issue-tracker.md`.

### Triage labels

The five default roles, recorded as `Status:` lines: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` at the repo root, ADRs in `.scratch/adr/` (git ignores them), alongside the design docs in `docs/`. See `docs/agents/domain.md`.
