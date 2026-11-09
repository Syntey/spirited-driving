# Map screen

> **Status:** draft, being iterated. Last updated 2026-11-09.
>
> The screen for the [Map](README.md) feature. The search step of its sheet is designed in
> [Destination search](../destination-search/design.md). Uses the [Theme](../../theme.md); how the screen is built
> per platform is in [Architecture](../../architecture.md#how-the-map-screen-is-built).

## Layout

A full-screen map with four things on it:

- **The sheet** at the bottom. Collapsed, it holds the search field, full width.
- **The profile button** in the top right corner, with a person icon, signed in or not
  ([Account](../account/design.md)). It's used rarely, so it can sit out of the thumb's reach.
- **The locate button**, where each platform's own maps app has it.
- **The covered area**: outside it, the map is dimmed.

|                 | iOS                                                     | Android                                               |
|-----------------|---------------------------------------------------------|-------------------------------------------------------|
| Sheet           | Native sheet, Liquid Glass, inset from the screen edges | Standard bottom sheet, `surfaceContainerLow`, full width |
| Search field    | Capsule, 48 pt, system fill                             | Search bar, 56 dp, `surfaceContainerHigh`             |
| Profile button  | Top right, glass circle                                 | Top right, `surfaceContainerHigh` circle              |
| Locate button   | Top right, below the profile button, glass circle       | Bottom right, above the sheet, moves with it          |

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

## Locate button

| State                | iOS                       | Android                 | Tap                                        |
|----------------------|---------------------------|-------------------------|--------------------------------------------|
| Following the user   | Filled arrow, primary     | Filled target, primary  | Nothing                                    |
| Not following        | Outline arrow, primary    | Outline target, grey    | Recenter and follow                        |
| No location access   | Arrow with a slash, grey  | Target with a slash, grey | Ask for access, or open the system settings |

Panning the map stops following. The map shows the user's heading on the location dot, and doesn't rotate with
it.

## Map

- **Base map, muted** so route lines stand out: MapKit's muted emphasis style, Google Maps with a JSON style.
  Business and transit points of interest are hidden. Dark follows the theme.
- **Covered area:** everything outside Czechia is dimmed (`scrim`: black at 20% in light, 50% in dark), with a
  thin border line along the edge. It shows at every zoom level.
- **Map padding** equals the collapsed sheet's height, so the map's logo and legal notice stay visible above the
  sheet.
- **Long-press** a point: haptic tap, a pin drops, and the sheet moves to
  [Route planning](../route-planning/README.md) for that point. While the user is choosing the start, it becomes
  the start instead. Its label comes from a reverse lookup
  ([Destination search](../destination-search/design.md#points-picked-on-the-map)).

## States

- **No location access:** the map opens on the whole covered area, with no location dot. The locate button shows
  the slash. Nothing else changes.
- **Approximate location only:** the location dot is a wide circle. A note in the sheet, on `tertiaryContainer`,
  says "Precise location is off" and "Turn it on to start routes where you are.", with **Turn on** (filled,
  `tertiary`), which shows the system prompt for precise location. Until then, the start of a trip is picked by
  hand, as with no location access.
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
| Search field     | Where to?                             |
| Saved routes, signed out | Sign in to keep routes for later |
| Saved routes, none yet | Save routes to drive later |
| Approximate location   | Precise location is off / Turn it on to start routes where you are. / Turn on |
