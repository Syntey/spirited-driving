# Drive

> **Status:** draft, MVP. Last updated 2026-11-08.

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
