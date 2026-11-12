# Destination search screens

> **Status:** partly built, being iterated. Last updated 2026-11-12.
>
> What's left to build of [Destination search](README.md): the search step of the map's sheet, for the destination and
> for the start. The field, searching, suggestions and their states are built; the decisions behind them are in the
> README's [Design](README.md#design) section. Uses the [Theme](../../theme.md); the map, its sheet and the sheet's heights are described in the
> [Map screen](../map/design.md).

## Two uses of one step

The same step finds the destination and, from [Route planning](../route-planning/design.md), the start. They
differ only in these:

|                  | Destination                                     | Start                                                        |
|------------------|-------------------------------------------------|--------------------------------------------------------------|
| Opens from       | The field on the map's collapsed sheet          | "From" in route planning, or choosing a destination without a usable location |
| Above the field  | Nothing                                         | "To Karlštejn Castle": the destination's name                |
| Field            | Where to?                                       | Where from?                                                  |
| First row        | Saved routes ([Map](../map/design.md#half-saved-routes-and-recent-places)) | My location ([below](#my-location))   |
| Touch and hold the map | Picks the destination                     | Picks the start                                              |
| Choosing a place | Opens route planning                            | Returns to route planning, which plans again                 |
| Close and Back   | The collapsed sheet, text cleared               | Where it opened from, start unchanged                        |

The start step opens with the keyboard up and the field empty. Opened because there was no usable location, it
comes straight after choosing a destination, with the destination's pin on the map; Close and Back return to the
destination search as it was.

## Recent places

Shown while the field is empty, under the header "Recent".

- **Each row:** a clock icon, the name, and "28 km · Karlštejn, Central Bohemia": the distance from the user and
  the area.
- **Order:** last chosen first. Choosing a place that is already there moves it to the top; the same place means
  the same name within 100 m.
- Up to 15 are kept and all are shown; the sheet scrolls.
- **Swipe left** on a row removes it, with no undo: searching again brings it back.
- **None yet:** a hint takes their place, so the empty sheet teaches the gesture the map doesn't show: "Search for
  a place, or touch and hold the map to pick a point."

## Suggestions

- **Order:** up to 3 recent places that match the text, then the backend's suggestions, leaving out any that already
  show as a recent place.
- **Matching recent places:** their name or area has a word that starts with what was typed, ignoring case and
  diacritics. They show at once and offline, with a clock for their icon.

## Searching

- With 1 character, only matching recent places show.
- After 0.5 s without an answer, a small progress indicator replaces the clear button.
- Without location access, the centre of the map orders places with the same name, nearest first, as the user's
  location does.

## My location

The first row of the start step, with a location arrow icon.

| Location                    | Row                                           | Tap                                                 |
|-----------------------------|-----------------------------------------------|-----------------------------------------------------|
| Precise, in the covered area | My location                                  | Makes it the start                                  |
| Approximate only            | My location / Needs precise location          | Shows the system prompt for precise location; once given, makes it the start |
| No access                   | My location / Needs location access           | Asks for access, or opens the system settings       |
| Outside the covered area    | My location / Outside the covered area, dimmed | Shows "Routes start and end in Czechia for now."   |

## Points picked on the map

Touching and holding the map picks a point ([Map](../map/design.md#picking-a-point)). Its label comes from a reverse lookup:

- While it loads, the point is called "Point on the map".
- The nearest address then becomes its name, and its area the town and region.
- With no address nearby, or when the lookup fails, it stays "Point on the map", with its coordinates as the area:
  "49.9394, 14.1882".

## States

- **Offline:** recent places still show, matching ones too, above the offline layout.
- **No results:** the text adds "or touch and hold the map to pick a point" once picking a point exists.

## Coming back

- Closing route planning returns to the destination search as it was: the text and its suggestions, at full height
  without the keyboard, so choosing the other Karlštejn takes one tap.
- Starting a drive clears the text, so the search is empty after the drive.

## Platforms

- Swipe to remove is the same Compose code on both ([Architecture](../../architecture.md#shared-and-native-ui)).

## Copy

| Where                  | Text                                                           |
|------------------------|----------------------------------------------------------------|
| Field, start           | Where from?                                                    |
| Above the start field  | To Karlštejn Castle                                            |
| Recent header          | Recent                                                         |
| No recent places       | Search for a place, or touch and hold the map to pick a point. |
| My location            | My location                                                    |
| My location, notes     | Needs precise location / Needs location access / Outside the covered area |
| Point on the map       | Point on the map                                               |
| No results, with picking a point | Check the spelling, or touch and hold the map to pick a point. |
