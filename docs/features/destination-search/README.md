# Destination search

> **Status:** partly built, MVP. Last updated 2026-11-12.

Finding a place by typing, as the destination or the start of a trip. Its field is part of the
[Map](../map/README.md), and [Route planning](../route-planning/README.md) uses the same search to change the start.

Most searches are for a place the user already knows, so search is built to find it by name, wherever it is.

## What the user can do

- See suggestions while typing, best match first, each with an icon for its kind: a town, a street, an
  address, nature (a peak, a pass, a lake) or another place, such as a castle.
- Type without diacritics: "karlstejn" finds Karlštejn.
- See recent places when the field is empty, and the ones matching the text first while typing.
- Remove a recent place.
- Choose a suggestion or a recent place as the destination or the start, or My location as the start.
- Pick the start on the map while choosing it, the same way the destination can be picked.

## Rules

- Recent places are kept on the device, last used first, up to 15. They hold destinations, starts picked by hand
  and points picked on the map. Choosing a place again moves it to the top.
- A point picked on the map gets the nearest address as its label.
- Suggestions come best match first: how well the name matches and how well known the place is. Where the user
  is only decides between places with the same name, such as streets or shops found in many towns: the nearest
  comes first. Without location access, the centre of the map decides.
- Search ignores diacritics and case, in suggestions and recent places alike.
- Place names come in the phone's language where the map data has them, otherwise in the local language, as on the
  road signs. The app's own text stays English.
- Places outside the covered area are listed, but can't be chosen.
- My location can be the start only with precise location, inside the covered area. Without that, choosing a
  destination asks for the start before planning.
- Recent places work offline; suggestions need a connection.

## Needs and provides

- **Needs:** units from [Settings](../settings/README.md) for the distances.
- **Provides:** a place (name, area, coordinates) for [Route planning](../route-planning/README.md), as the
  destination or the start.

## Design

The search is a step of the map's sheet: `TripPlanComponent` in `:feature:map` holds it, and `TripPlanSheetUi` draws
the field and the suggestions on both platforms. The code holds its layout and copy; these are the decisions behind
it. What isn't built yet, recent places, the start and points picked on the map, is still in [design.md](design.md).

- **Tapping the field moves the sheet to full height,** with the keyboard. Dragging the sheet lower or scrolling the
  list hides the keyboard and keeps the text. Close (X on iOS, the back arrow or Back on Android) clears the text and
  collapses the sheet.
- **Search on the keyboard only hides it.** Choosing the first suggestion for the user could plan a route to the
  wrong place.
- **Autocorrect is off,** so it doesn't change place names.
- **The app searches from 2 characters, after a 300 ms pause in typing,** so each keystroke doesn't reach the backend.
  A new search cancels the one still waiting or running, and the previous suggestions stay until its answer comes, so
  the list doesn't flash.
- **The field keeps its own copy of the text,** so typing never waits for the view state. Only clearing comes from
  outside: Clear, Close and choosing a place.
- **Until recent places exist, the empty field shows "Search for a place."** The designed hint also teaches touching
  and holding the map, which comes with [Route planning](../route-planning/README.md).
- **Matching ignores case and diacritics,** with one character folded to one, so the bold parts of a name line up with
  what was typed: "karlst" bolds "Karlšt". A word matches from its start only.
- **Distances are straight lines from the user** in the units of the phone's region, miles in the US and the UK, until
  Settings lets the user choose. With approximate location they're whole kilometres or miles, and without location
  access they're left out.
- **Calls carry the phone's languages as `Accept-Language`,** on Android too, so place names come in the user's
  language where the map data has it.
- **A slow search shows the car spinner** in place of the clear button after 0.5 s, so nothing in the field moves.
- **Offline and failed searches say which it is,** each with Try again; no results has no button, as searching again
  means changing the text. Until picking a point exists, its text doesn't mention touching and holding the map.
- **Until Route planning exists, choosing a place only drops its pin** on the [Map](../map/README.md#design). A place
  outside the covered area is dimmed, and tapping it explains that routes start and end in Czechia for now.
- **Ordering places with the same name by the map's centre,** without location access, comes in a later slice; the
  user's location already orders them.

## Account

Not needed.

## Backend

Search places, reverse lookup.
