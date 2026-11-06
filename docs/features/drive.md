# Drive

> **Status:** draft, MVP. Last updated 2026-11-05.

Hands a route to the phone's maps app, which guides the drive turn by turn.

## What the user can do

- Start a drive on a route. The app opens Google Maps (Android) or Apple Maps (iOS) with the route's waypoints and
  starts navigation.

## Rules

- The route's start is its first waypoint, so the maps app guides the user there when they aren't already. Most
  routes start at the user's location.
- On Android, the app shows who provides the routing before the hand-off, as Google's terms require.
- Every drive started is recorded.

## Needs and provides

- **Needs:** a route from [Route planning](route-planning.md), [Saved routes](saved-routes.md) or
  [Trip history](trip-history.md).
- **Provides:** recorded drives for [Drive feedback](drive-feedback.md) and [Trip history](trip-history.md).

## Account

Not needed.

## Backend

Record a drive.
