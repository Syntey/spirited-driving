---
paths:
  - "**/src/commonTest/**"
  - "**/src/androidHostTest/**"
  - "**/src/iosTest/**"
---

# Tests

- What must have tests: use cases, mapping (DTO ↔ domain, domain → view state), and pure domain logic such as
  polyline decoding, building the hand-off link, and matching search text. Compose UI is checked with previews,
  Koin wiring by running the app.
- Tests go in the `commonTest` of the module under test and use `kotlin.test`. They run on the JVM:
  `./gradlew :<module>:testAndroidHostTest` (CI runs `testAndroidHostTest`).
- For `suspend` code, add `libs.kotlinx.coroutines.test` to the module's `commonTest` dependencies and use `runTest`.
- Hand-written fakes of `:core:domain` interfaces, no mocking library. Reuse the `Fake*` in `:core:domain` when it
  fits.
- Names are backticked sentences about behavior: `` fun `plan without a fun route keeps the fastest one`() ``. Plain
  words only: Kotlin/Native rejects some punctuation in names.
- One behavior per test. Build inputs inline or with a small helper in the test file, not a shared fixture class.
