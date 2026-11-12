# Map screen

> **Status:** partly built, being iterated. Last updated 2026-11-12.
>
> What's left to build of the [Map](README.md) screen. The map, its profile and locate buttons, the covered area, its
> notes and the collapsed sheet with the search field are built; the decisions behind them are in the README's
> [Design](README.md#design) section.
> The search step of the sheet is designed in [Destination search](../destination-search/design.md). Uses the
> [Theme](../../theme.md); how the screen is built per platform is in
> [Architecture](../../architecture.md#how-the-map-screen-is-built).

## Sheet heights

| Height    | Shows                                                                | When                                    |
|-----------|----------------------------------------------------------------------|-----------------------------------------|
| Collapsed | Search field                                                         | On launch, and after closing the search |
| Half      | Saved routes row and recent places                                   | Dragged up                              |
| Full      | Saved routes row and recent places, or suggestions, with the keyboard | Tapping the field                       |

On Android, the locate button hides while the sheet is above collapsed.

### Half: Saved routes and recent places

- **Saved routes** row: icon on `primaryContainer`, title, and a second line. Signed in: the number of routes, or
  "Save routes to drive later" when there are none.
  Signed out: "Sign in to keep routes for later", and tapping it asks the user to sign in
  ([Navigation](../../navigation.md#sign-in)).
- **Recent places**, and the suggestions that replace them while typing, are in
  [Destination search](../destination-search/design.md).

## Picking a point

**Long-press** a point: haptic tap, a pin drops, and the sheet moves to
[Route planning](../route-planning/README.md) for that point. While the user is choosing the start, it becomes the
start instead. Its label comes from a reverse lookup
([Destination search](../destination-search/design.md#points-picked-on-the-map)).

## Copy

| Where            | Text                                  |
|------------------|---------------------------------------|
| Saved routes, signed out | Sign in to keep routes for later |
| Saved routes, none yet | Save routes to drive later |
