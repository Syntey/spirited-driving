---
name: platform-binding
description: Add a device or SDK service that each platform implements natively (location, Firebase sign-in and App Check, local notifications, opening another app) - the interface in :core:platform, the Android and iOS implementations, and the Koin binding. Use whenever shared code needs something only the platform SDKs provide, even if the user only says "get the user's location".
---

# Platform binding

Shared code reaches native SDKs through interfaces in `:core:platform`, implemented in `androidApp` (Kotlin) and
`iosApp` (Swift). `Platform` and `PlatformFirebaseCrashlytics` are the existing examples.

## Steps

1. **Interface**: `core/platform/src/commonMain/kotlin/.../core/platform/binding/Platform<Name>.kt`, with KDoc on each
   member saying what it does and how a failure shows.
   - Plain Kotlin types, and small data classes defined next to it. No platform types.
   - Swift implements it, so keep to what Swift implements cleanly: properties, plain functions, and callbacks through
     a listener interface. Turn callbacks into `suspend` functions or a `Flow` on the Kotlin side
     (`suspendCancellableCoroutine`, `callbackFlow`), in a class in `:core:platform` that wraps the binding.
   - Only when the platforms need different signatures (Crashlytics takes a `Throwable` on Android and an
     `NSException` on iOS): `expect interface` in `commonMain`, `actual interface` in `androidMain` and `iosMain`.
2. **Accessor**: `fun <name>(): Platform<Name>` in `PlatformBindings`.
3. **Koin**: `single<Platform<Name>> { platformBindings.<name>() }` in `platformModule`
   (`core/platform/.../injection/PlatformModule.kt`), and the wrapper from step 1 next to it. This module has no
   Koin compiler plugin, so `@Single` and `@Factory` do nothing here.
4. **Android**: `androidApp/src/main/kotlin/.../android/tools/binding/Platform<Name>Impl.kt`, returned from
   `PlatformBindingsImpl`, which also has the application `Context`.
5. **iOS**: `iosApp/SpiritedDriving/Platform/Platform<Name>Impl.swift` (`final class Platform<Name>Impl:
   Platform<Name>`), returned from `PlatformBindingsImpl.swift`. Rebuild the Kotlin framework first, or Swift won't
   see the new interface.
6. **Setup**: SDK dependencies (Android in `gradle/libs.versions.toml`, iOS as a Swift package in the Xcode project),
   Android permissions in `androidApp/src/main/AndroidManifest.xml`, and iOS usage descriptions in
   `iosApp/SpiritedDriving/Info.plist`. Asking for a permission belongs to the binding, and the result tells shared
   code what to do next: granted, denied, or denied for good.
7. **Use**: inject the interface (or its wrapper) into a use case or a `:data:*` class. A feature module that uses it
   adds `implementation(projects.core.platform)` to its build file. Tests use a fake from the test source set.

## Before you call it done

- `./gradlew lintCheck` and `./gradlew testAndroidHostTest` pass.
- Both apps build (commands in `CLAUDE.md`).
- It works on an Android device or emulator and on an iOS simulator. Tests can't reach the native side, so this is
  the only check it gets.
