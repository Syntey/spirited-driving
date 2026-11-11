# Destination search screens

> **Status:** draft, being iterated. Last updated 2026-11-09.
>
> The design for [Destination search](README.md): the search step of the map's sheet, for the destination and for
> the start. Uses the [Theme](../../theme.md); the map, its sheet and the sheet's heights are described in the
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

## Field

- Tapping the field focuses it: the keyboard comes up and the sheet moves to full height. Empty, the sheet shows
  the first row and the recent places; typing replaces them with suggestions. Dragging the sheet to half height
  shows the same without the keyboard.
- A clear button inside the field while it has text.
- The keyboard's return key reads Search and only hides the keyboard; the list stays. It doesn't choose the first
  suggestion: a wrong guess would plan a route to the wrong place.
- Autocorrect is off, so it doesn't change place names.
- Scrolling the list hides the keyboard.

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

Shown while the field has text.

- **Order:** up to 3 recent places that match the text, then the backend's suggestions, best match first, leaving
  out any that already show as a recent place.
- **Matching recent places:** their name or area has a word that starts with what was typed, ignoring case and
  diacritics. They show at once and offline.
- **Each row:** an icon for the place's kind (a clock for a recent place), the name, and the distance and area as
  in [Recent places](#recent-places).
- **Bold:** the parts of the name that match the typed words, from the start of a word, ignoring case and
  diacritics ("karlst" bolds "Karlšt"). A name that matched in another language, such as "Prague" for "praha", has
  nothing bold.
- **Kinds:**

  | Kind              | Icon     | Examples                              |
  |-------------------|----------|---------------------------------------|
  | Town              | Buildings | Cities, towns, villages, districts   |
  | Street            | Road     | Streets without a house number        |
  | Address           | House    | A street or village and house number  |
  | Nature            | Mountain | Peaks, passes, viewpoints, lakes      |
  | Place of interest | Pin      | Castles, shops, car parks; any kind the app doesn't know |

  The icons sit in a circle on `secondaryContainer`.
- **Not covered:** places outside the covered area are dimmed, and "Not covered yet" takes the distance's place:
  "Not covered yet · Dresden, Saxony". Tapping one shows "Routes start and end in Czechia for now." in a message
  capsule (iOS) or snackbar (Android).
- Up to 10 suggestions come from the backend.

**Distances** are straight lines from the user, in the units from Settings, with tabular figures: "850 m" under
1 km (in steps of 50 m), "4.2 km" under 10 km, "28 km" from there; in miles, "0.3 mi", "4.2 mi", "17 mi". With
approximate location they are whole km or miles. Without location access the distance is left out.

## Searching

- The app searches from 2 characters, after a 300 ms pause in typing. With 1 character, only matching recent
  places show.
- A new search cancels the one still running, and only the answer for the current text is shown.
- While a search runs, the previous suggestions stay, so the list doesn't flash. After 0.5 s without an answer, a
  small progress indicator replaces the clear button.
- Best match first. The user's location, precise or approximate, only orders places with the same name, such as
  "Nádražní" or "Lidl", nearest first; without location access, the centre of the map does.

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

These use the [empty and error layout](../../theme.md#empty-and-error-states) in place of the suggestions, below
the field:

- **Offline:** recent places still show, matching ones too, with the layout below them: "You're offline" and
  "Search needs a connection.", with Try again.
- **Search failed** (server error or too many requests): "Search isn't working right now" and "The problem is on
  our side, not your connection.", with Try again.
- **No results:** "No places match "karlsxyz"" and "Check the spelling, or touch and hold the map to pick a
  point." No button: searching again means changing the text.

## Coming back

- Closing route planning returns to the destination search as it was: the text and its suggestions, at full height
  without the keyboard, so choosing the other Karlštejn takes one tap.
- Closing the search (X on iOS, Back on Android) clears the text and collapses the sheet.
- Starting a drive clears the text, so the search is empty after the drive.

## Platforms

|                | iOS                                           | Android                                         |
|----------------|-----------------------------------------------|-------------------------------------------------|
| Field          | Capsule, 48 pt, system fill (the sheet's glass shows through) | Search bar, 56 dp, `surfaceContainerHigh` |
| Close          | X button next to the field                    | Back arrow at the start of the field            |
| Rows           | Rows on a translucent card, corners 22 pt     | List items, 72 dp, on the sheet                 |
| Kind icons     | 30 pt circle                                  | 40 dp circle                                    |
| Not covered tapped | Capsule on `inverseSurface`               | Snackbar                                        |

- **Built with:** shared Compose, styled per platform as above. Swipe to remove is the same Compose code on both
  ([Architecture](../../architecture.md#shared-and-native-ui)).
- Matching and bolding ignore diacritics in common code, with unit tests.
- Screen readers hear a row as "Karlštejn Castle, place of interest, 28 kilometres, Karlštejn, Central Bohemia",
  and a dimmed one with "not covered yet".

## Copy

| Where                  | Text                                                           |
|------------------------|----------------------------------------------------------------|
| Field, destination     | Where to?                                                      |
| Field, start           | Where from?                                                    |
| Above the start field  | To Karlštejn Castle                                            |
| Recent header          | Recent                                                         |
| No recent places       | Search for a place, or touch and hold the map to pick a point. |
| Row                    | 28 km · Karlštejn, Central Bohemia                             |
| Not covered            | Not covered yet · Dresden, Saxony                              |
| Not covered, tapped    | Routes start and end in Czechia for now.                       |
| My location            | My location                                                    |
| My location, notes     | Needs precise location / Needs location access / Outside the covered area |
| Point on the map       | Point on the map                                               |
| Offline                | You're offline / Search needs a connection.                    |
| Search failed          | Search isn't working right now / The problem is on our side, not your connection. |
| No results             | No places match "karlsxyz" / Check the spelling, or touch and hold the map to pick a point. |
| Retry                  | Try again                                                      |
