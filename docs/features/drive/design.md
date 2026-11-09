# Drive

> **Status:** draft, being iterated. Last updated 2026-11-07.
>
> The design around [Drive](README.md). Drive has no screen of its own: it starts from the Drive button
> in [Route planning](../route-planning/design.md) and opens the phone's maps app. Uses the [Theme](../../theme.md).

## Drive button

- Filled with primary, the wider of the two buttons at the bottom of the sheet, labelled "Drive".
- Tapping it opens Apple Maps (iOS) or Google Maps (Android) with the route's waypoints, with navigation started.
- The sheet stays on the route, so the user finds it there when they come back. Once the drive is likely over,
  [Drive feedback](../drive-feedback/design.md) takes its place.

## Routing provided by (Android)

Google's EEA terms: when a route not made by Google is shown on or alongside a Google map, the UI must clearly show
"Routing provided by [provider]" before navigation begins.

- Every Android sheet that shows routes on the map has "Routing provided by Spirited Driving" under the Drive
  button, above the OpenStreetMap attribution, whether the sheet is at half height or collapsed.
- No dialog: the line is on screen before Drive is tapped.

## First drive

Before the first hand-off only, a dialog explains what happens next. Both maps apps show the route as a few stops,
which looks odd without a word of warning.

|          | iOS                                     | Android                                 |
|----------|-----------------------------------------|-----------------------------------------|
| Form     | Native alert                            | Material dialog with a car icon         |
| Title    | Your drive continues in Apple Maps      | Your drive continues in Google Maps     |
| Actions  | Open Apple Maps (prominent), Cancel     | Cancel, Open Google Maps                |

Message: "[Apple Maps / Google Maps] gets the route as a few stops along the way. Drive through each one to stay
on the fun roads."

## Route that starts elsewhere

When the route's start is more than about 1 km from the user, as with a saved route or a start picked by hand, a
line above Drive says: "Starts 12 km from you. [Apple Maps / Google Maps] takes you there first." Drawn with
[Saved routes](../saved-routes/design.md#a-saved-route-on-the-map).

## Maps app missing

Apple Maps can be deleted, and Google Maps can be turned off. Tapping Drive then shows:

|          | iOS                                                                  | Android                                                                     |
|----------|----------------------------------------------------------------------|-----------------------------------------------------------------------------|
| Title    | Apple Maps isn't on this iPhone                                      | Google Maps is needed                                                       |
| Message  | Drives open in Apple Maps. Get it from the App Store, then tap Drive again. | Drives open in Google Maps. Install it or turn it on, then tap Drive again. |
| Actions  | Get Apple Maps (prominent), Cancel                                   | Cancel, Open Play Store                                                     |

## Open questions

1. **How each maps app treats a stop:** whether it pauses there or asks to continue. The first-drive message
   depends on it; see [Architecture, test cases](../../architecture.md#test-cases).
