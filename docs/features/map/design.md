# Map screen

> **Status:** partly built, being iterated. Last updated 2026-11-16.
>
> What's left to build of the [Map](README.md) screen: the content of the half-height sheet. The rest is built; the
> decisions behind it are in the README's [Design](README.md#design) section.
> The search step of the sheet is designed in [Destination search](../destination-search/design.md). Uses the
> [Theme](../../theme.md); how the screen is built per platform is in
> [Architecture](../../architecture.md#how-the-map-screen-is-built).

## Half: Saved routes and recent places

Dragged up to half height with the field empty, the sheet shows:

- **Saved routes** row: icon on `primaryContainer`, title, and a second line. Signed in: the number of routes, or
  "Save routes to drive later" when there are none.
  Signed out: "Sign in to keep routes for later", and tapping it asks the user to sign in
  ([Navigation](../../navigation.md#sign-in)).
- **Recent places**, and the suggestions that replace them while typing, are in
  [Destination search](../destination-search/design.md).

## Copy

| Where            | Text                                  |
|------------------|---------------------------------------|
| Saved routes, signed out | Sign in to keep routes for later |
| Saved routes, none yet | Save routes to drive later |
