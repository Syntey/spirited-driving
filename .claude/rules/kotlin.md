---
paths:
  - "**/*.kt"
---

# Kotlin

- `internal` unless another module uses it.
- Immutable by default: `val`, `data class` for values, kotlinx `ImmutableList`/`ImmutableMap` in state.
- No `!!`. Handle null with `?:`, `?.let`, or `requireNotNull(x) { "why it can't be null" }`.
- No `GlobalScope` or `runBlocking` in production code. Components run work through use cases (`execute`), which
  are cancelled with the component. Work that has to finish after its screen closes goes to `AppWork` from
  `:core:domain`.
- Platform code goes in `androidMain`/`iosMain` with `.android.kt`/`.ios.kt` file names. Prefer an interface in
  common code to `expect`/`actual`.
- A module's fixed values (option lists, defaults, limits, delays, timeouts, backend codes) go in its
  `tools/Constants.kt`, in nested objects (`Constants.Search.MIN_LENGTH`), not in the file that uses them. Unit
  conversions and formatting arithmetic (`MINUTES_PER_HOUR`) stay private where they're used.
- Comments say why, when the code can't. No comments that restate the code or describe the change you made.
- Import moko resources with `com.syntey.spiriteddriving.core.resources.*`, not one import per resource.
