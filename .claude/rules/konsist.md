---
paths:
  - "konsist/**"
---

# Konsist rules

Rules the compiler, detekt and `moduleGraphAssert` can't check: something that compiles but breaks a convention or
fails at runtime. Don't add a rule the compiler already enforces.

- One test class per area (`ScreenKonsistTest`, `DataKonsistTest`, …). Every assertion passes a `KoRule` with `id`,
  `title`, `reason` and `howToFix`: the failure message is what tells the reader how to fix it.
- Use the shared `productionScope` from `Scope.kt`. Match annotations by name (`hasAnnotationWithName("Factory")`),
  since the inspected libraries aren't on this module's classpath.
- A new rule must fail on a deliberate violation before you trust it. A filter that matches nothing passes.
- Konsist 1.0 traps: a parent's `name` includes its type arguments (use `simpleName` from `Scope.kt`), and a
  parent's `packagee` is the package of the file that declares the child, not of the parent. Recognize a parent from
  another module by the file's imports.
- An exception to a rule is an in-rule filter with a comment saying why.
- `./gradlew :konsist:test` runs the rules; `lintCheck` includes it.
