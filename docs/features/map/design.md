# Map screen

> **Status:** partly built, being iterated. Last updated 2026-11-11.
>
> What's left to build of the [Map](README.md) screen. The map, its profile and locate buttons, and the collapsed sheet
> with the search field are built; the decisions behind them are in the README's [Design](README.md#design) section.
> The search step of the sheet is designed in [Destination search](../destination-search/design.md). Uses the
> [Theme](../../theme.md); how the screen is built per platform is in
> [Architecture](../../architecture.md#how-the-map-screen-is-built).

## Covered area

- Everything outside Czechia is dimmed (`scrim`: black at 20% in light, 50% in dark), with a thin border line along
  the edge. It shows at every zoom level.
- The covered area comes from the backend. Without location access the map opens on it; until it's loaded, the map
  opens on Czechia's corners, as it does now.

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

## States

- **Approximate location only:** the location dot is a wide circle (both map SDKs draw it so). A note in the sheet, on
  `tertiaryContainer`, says "Precise location is off" and "Turn it on to start routes where you are.", with **Turn on**
  (filled, `tertiary`), which shows the system prompt for precise location. Until then, the start of a trip is picked
  by hand, as with no location access.
  The note has a close button, and once closed it doesn't show again: approximate location can be the user's
  choice. The search for the start still offers precise location in its My location row
  ([Destination search](../destination-search/design.md#my-location)).
- **Outside the covered area:** the map opens zoomed out to show both the user and the covered area, and the
  locate button shows "not following". A note in the sheet, on `tertiaryContainer`, says: "You're outside the
  covered area. Routes start and end in Czechia for now. Search for a destination there and pick the start by
  hand."

## Copy

| Where            | Text                                  |
|------------------|---------------------------------------|
| Saved routes, signed out | Sign in to keep routes for later |
| Saved routes, none yet | Save routes to drive later |
| Approximate location   | Precise location is off / Turn it on to start routes where you are. / Turn on |
