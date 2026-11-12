# Destination search screens

> **Status:** partly built, being iterated. Last updated 2026-11-12.
>
> What's left to build of [Destination search](README.md): the search step of the map's sheet for the start. The
> search for the destination is built, picking a point on the map too; the decisions behind it are in the README's
> [Design](README.md#design) section. Uses the [Theme](../../theme.md); the map, its sheet and the sheet's heights are described in the
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

Built for destinations and points picked on the map ([Design](README.md#design)). Still to come: starts picked by
hand are kept too.

## Searching

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

Picking a point is built ([Design](README.md#design)). While the start step is open, touching and holding the map picks
the start instead. Until a point's lookup answers, route planning's header calls it "Point on the map".

## Coming back

- Closing route planning returns to the destination search as it was: the text and its suggestions, at full height
  without the keyboard, so choosing the other Karlštejn takes one tap.
- Starting a drive clears the text, so the search is empty after the drive.

## Copy

| Where                  | Text                                                           |
|------------------------|----------------------------------------------------------------|
| Field, start           | Where from?                                                    |
| Above the start field  | To Karlštejn Castle                                            |
| My location            | My location                                                    |
| My location, notes     | Needs precise location / Needs location access / Outside the covered area |
