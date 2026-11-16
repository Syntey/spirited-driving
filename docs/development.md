# Spirited Driving: Development

> How to build, run and check the app. How it's designed is in [architecture.md](architecture.md).

## Gradle tasks

| Task                                  | What it does                                                                                       |
|---------------------------------------|----------------------------------------------------------------------------------------------------|
| `lintCheck`                           | ktlint, detekt, Android lint and the Konsist architecture rules (`konsist/`); any error fails it   |
| `testAndroidHostTest`                 | Every shared module's tests on the JVM, as CI runs them                                            |
| `ktlintFormat`                        | Reformat code to the ktlint rules                                                                  |
| `generateMRcommonMain`                | Regenerate resource accessors after editing `core/resources/.../strings.xml`                       |
| `generateModulesGraphvizText`         | Print the current module graph                                                                     |
| `assembleAndCopyDebugSwiftPackage`    | Build the debug KMP XCFramework and copy it into the local Swift Package                           |
| `assembleAndCopyReleaseSwiftPackage`  | The same for release                                                                               |

CI runs `lintCheck` and `testAndroidHostTest` on every pull request (`.github/workflows/on_pull_request.yml`).

## Configuration

Keep these in `local.properties`, which git ignores:

```properties
# Maps SDK for Android key. Without it the app runs, but the Android map stays blank.
MAPS_API_KEY=your-key
# Backend address for dev builds on an Android phone on the same Wi-Fi
ANDROID_DEV_API_URL=http://192.168.1.20:8080/v1/
```

CI passes the Maps key as the `MAPS_API_KEY` environment variable. The iOS map (MapKit) needs no key.

**Firebase** (sign-in, App Check, Crashlytics) reads `google-services.json` on Android and
`GoogleService-Info.plist` on iOS. Both stay out of git. Without them the app runs signed out, and Crashlytics
calls are skipped.

### Backend

The backend is a separate project behind an HTTP API. Dev builds call it on your computer at port 8080: the
Android emulator reaches it at `10.0.2.2`, the iOS simulator at `localhost`, and an Android phone at
`ANDROID_DEV_API_URL`.

### Product flavors

[BuildKonfig](https://github.com/yshrsmz/BuildKonfig) selects the API URL in `:data:network` for two flavors,
`dev` and `prod`. Pass the flavor as a Gradle property, or set `buildkonfig.flavor=dev` in `gradle.properties` for
local Android work:

```shell
./gradlew whateverTask -P buildkonfig.flavor=dev
```

Debug and enterprise Android builds trust user-installed certificates, so a proxy such as
[Charles](https://www.charlesproxy.com/) can inspect their traffic.

## iOS

### Swift Package

The iOS app reaches the Kotlin code through a local Swift Package at `iosApp/shared/KMP/`, which wraps the
XCFramework Gradle builds. The "Build KMP" target in Xcode runs [the Makefile](../iosApp/shared/KMP/Makefile), which
picks the Gradle task from these build settings, set in the `.xcconfig` files:

| Setting                    | Values                          | Gradle property                     |
|----------------------------|---------------------------------|-------------------------------------|
| `KMP_FRAMEWORK_BUILD_TYPE` | `debug`, `release`              | picks the `assembleAndCopy*` task   |
| `KMP_BUILD_FLAVOR`         | `dev`, `prod`                   | `-P buildkonfig.flavor`             |
| `KMP_FRAMEWORK_TARGETS`    | `simulator`, `device`, `all`    | `-PkmpFrameworkTargets`             |
| `KMP_IS_STATIC`            | `true`, `false`                 | `-PisStatic`                        |

- Debug uses a dynamic framework, which SwiftUI previews need. Beta (TestFlight) and Release use a static framework
  built for devices only. Debug and Beta use the `dev` flavor, Release `prod`.
- `simulator` builds only `iosSimulatorArm64`, the fastest option for local work. Without a value, Gradle builds
  all targets, statically.

### First build

Xcode resolves the Swift Package only once the XCFramework exists, so build it once by hand:

```shell
./gradlew assembleAndCopyDebugSwiftPackage -PkmpFrameworkTargets=simulator -PisStatic=false
```

After that, build the "Build KMP" target whenever Kotlin code changes, then the app target. Xcode doesn't rebuild
the Kotlin framework by itself.

### Local overrides

To build only for the simulator, copy `iosApp/Config Files/Local.default.xcconfig` to `Local.xcconfig` next to it
and set `KMP_FRAMEWORK_TARGETS = simulator`. `Local.xcconfig` is gitignored, and only Debug reads it.

### Crash reports

Kotlin crashes reach Crashlytics on iOS symbolicated, through
[NSExceptionKt](https://github.com/rickclephas/NSExceptionKt). The framework is static in Beta and Release, so its
debug symbols are part of the app's own dSYM: upload the app's symbols as usual.
