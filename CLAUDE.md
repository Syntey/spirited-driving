# Spirited Driving Commands and Guidelines

## Common Commands
- `./gradlew clean` - Remove all build artifacts
- `./gradlew lintCheck` - Run ktlint and detekt checks
- `./gradlew ktlintFormat` - Automatically fix style issues
- `./gradlew test` - Run all tests
- `./gradlew :feature:<name>:testAndroidHostTest` - Run one feature module's tests
- `./gradlew generateMRcommonMain` - Regenerate shared resources

## iOS-specific Commands
- `cd iosApp/shared/KMP && make build KMP_FRAMEWORK_BUILD_TYPE=debug KMP_BUILD_FLAVOR=dev`
- `./gradlew assembleAndCopyDebugSwiftPackage` - Build debug XCFramework for SPM

## Code Style Guidelines
- **Architecture**: Decompose components with state, events, and navigation
- **Modules**: `:feature:*` and `:data:*` depend on `:core:*` only (the build enforces it); features reach data through `:core:domain` interfaces, and navigation hosts and Koin wiring live in `:shared`. See README "Modules"
- **Naming**: PascalCase for classes (*Component, *Screen, *UseCase, *ViewState)
- **Error Handling**: Use sealed NetworkError classes and results (Success/Failure)
- **Platform-specific code**: Use .android.kt and .ios.kt suffixes
- **Resources**: Import moko resources with `com.syntey.spiriteddriving.core.resources.*` (wildcard allowed by lint), not one import per resource
- **State**: Immutable data classes for state representation
- **Max limits**: 140 char line length, 25 functions per class, 20 per file
- **Testing**: Required for business logic and data transformations