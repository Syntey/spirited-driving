# Drive

> **Status:** built, MVP. Last updated 2026-11-13.

Hands a route to the phone's maps app, which guides the drive turn by turn.

## What the user can do

- Start a drive on a route. The app opens Google Maps (Android) or Apple Maps (iOS) with the route's waypoints and
  starts navigation.

## Rules

- The route's start is its first waypoint, so the maps app guides the user there when they aren't already. Most
  routes start at the user's location.
- On Android, the app shows who provides the routing before the hand-off, as Google's terms require.
- Every drive started is recorded. Starting the same route again before half its duration has passed continues
  the same drive, so tapping Drive twice doesn't record two drives.

## Needs and provides

- **Needs:** a route from [Route planning](../route-planning/README.md), [Saved routes](../saved-routes/README.md) or
  [Trip history](../trip-history/README.md).
- **Provides:** recorded drives for [Drive feedback](../drive-feedback/README.md) and [Trip
  history](../trip-history/README.md).

## Account

Not needed.

## Backend

Record a drive.

## Design

Drive has no screen of its own: it is the Drive button of [Route planning](../route-planning/README.md), held by
`TripPlanComponent` in `:feature:map`. The code holds its layout and copy; these are the decisions behind it.

- **The sheet stays on the route** after the hand-off, so the user finds it there when they come back.
- **"Routing provided by Spirited Driving" is always under Drive on Android,** above the OpenStreetMap attribution,
  in the half and the collapsed sheet. Google's EEA terms ask for it before navigation begins when routes not made by
  Google are shown on a Google map. A line on screen meets that without a dialog in the way.
- **A dialog comes before the first hand-off only:** both maps apps show the route as a few stops, which looks odd
  without a word of warning. Cancel keeps it for the next tap; only opening the maps app marks it as seen.
- **A route that starts more than about 1 km away** says so above Drive ("Starts 12 km from you. Google Maps takes you
  there first."), as the maps app guides the user to the start first.
- **A missing maps app is told apart from a failed link.** Android opens the link only in Google Maps, and iOS only as
  a universal link, so a deleted or turned off app shows a dialog that leads to the store instead of the link opening
  in a browser.
- **Apple Maps opens with the directions shown,** and the user taps Go: its links can't start navigation, while
  Google Maps' can. Its waypoint limit isn't documented, so it gets Google Maps' 9 until measured on a device.
- **The app makes each drive's ID** and keeps the drive on the device, so a drive started offline is recorded on the
  next start of the map. A drive whose plan expired before it reached the backend is dropped.

## Open questions

1. **How each maps app treats a stop:** whether it pauses there or asks to continue. The first-drive message
   depends on it; see [Architecture, test cases](../../architecture.md#test-cases).
