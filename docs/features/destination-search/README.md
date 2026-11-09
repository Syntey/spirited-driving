# Destination search

> **Status:** draft, MVP. Last updated 2026-11-08.

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

## Account

Not needed.

## Backend

Search places, reverse lookup.
