---
paths:
  - "**/src/commonTest/**"
  - "**/src/androidHostTest/**"
  - "**/src/iosTest/**"
---

# Tests

- What must have tests: use cases with logic, mapping (DTO ↔ domain, domain → view state), and pure domain logic
  such as polyline decoding, building the hand-off link, and matching search text. Compose UI is checked with
  previews, Koin wiring by running the app.
- A component whose actions move between steps or combine several use cases is tested through its Screen: actions in,
  view state, UI events and a recording fake of its navigation out. Build it with real use cases over fakes, in a
  `TestComponentContext` from `:core:ui` given the test's dispatcher, and never reach its child components directly.
- A use case that only passes its call on (`= auth.signOut()`) gets no test: it could only prove the fake was called.
  A default, a mapping, a filter or `distinctUntilChanged` is logic and gets one. Logic shared by use cases in several
  modules lives in `:core:domain` and is tested there once.
- Tests go in the `commonTest` of the module under test and use `kotlin.test`. They run on the JVM:
  `./gradlew :<module>:testAndroidHostTest` (CI runs `testAndroidHostTest`).
- For `suspend` code, add `libs.kotlinx.coroutines.test` to the module's `commonTest` dependencies and use `runTest`.
- Hand-written fakes of `:core:domain` interfaces, no mocking library. Reuse the `Fake*` in `:core:domain` when it
  fits.
- Names are backticked sentences about behavior: `` fun `plan without a fun route keeps the fastest one`() ``. Plain
  words; apostrophes and hyphens are fine, but no commas or `. ; : / \ < > [ ]`, which Android Studio or Kotlin/Native reject.
- One behavior per test. Build inputs inline or with a small helper in the test file, not a shared fixture class. The
  exception is a component under test: one fixture file per component builds it and exposes the fakes tests script,
  as building it inline would repeat dozens of use cases in every file.
