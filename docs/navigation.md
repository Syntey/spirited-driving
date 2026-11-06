# Spirited Driving: Navigation

> **Status:** draft, being iterated. Last updated 2026-11-05.
>
> How the user moves between [features](overview.md#features).

## Structure

The [Map](features/map.md) is the only main screen. There is no bottom navigation: a trip happens in a sheet over
the map, and everything else opens from it.

```
First launch (once)
   ↓
Map ── sheet: Search ─► Route planning ─► Drive hand-off
 │       ├─ Saved routes ─► Map with the route selected
 │       └─ Recent destinations ─► Route planning
 └─ Profile ─┬─ Trip history
             ├─ Settings
             └─ Account
```

## Map and its sheet

The map stays on screen while the user searches and plans. A sheet at the bottom shows the current step:

1. **Search.** Collapsed, the sheet shows the search field. Opened, it shows a Saved routes row with the recent
   destinations below it; typing replaces them with suggestions
   ([Destination search](features/destination-search.md)).
2. **Route planning.** Choosing a suggestion, a recent destination or a point long-pressed on the map opens
   [Route planning](features/route-planning.md) in the sheet, with the routes drawn on the map.
3. **One route.** A route chosen from Saved routes or Trip history shows on its own, with its details and Drive.
   There is no budget: the route isn't planned again.
4. **Drive.** Starting a drive hands off to the phone's maps app ([Drive](features/drive.md)).

Back goes one step: from route planning to search, from search to the map. Back on the map leaves the app
(Android).

## Saved routes

The Saved routes row opens [Saved routes](features/saved-routes.md) as a list. Choosing a route returns to the map
with that route on its own in the sheet, ready to drive.

Signed-out users see the row too, as a reminder to sign in.

## Profile

A profile button on the map opens Profile, which leads to:

- [Trip history](features/trip-history.md). Driving a route again returns to the map with that route on its own in
  the sheet; planning again opens Route planning with the same start and destination.
- [Settings](features/settings.md)
- [Account](features/account.md)

## Sign-in

Opening a feature that needs an account while signed out shows an alert saying the feature needs sign-in, with
**Sign in** and **Cancel**. After signing in, the feature opens. This covers the Saved routes row, saving a route
and Trip history.

## Prompts

- [First launch](features/first-launch.md) shows once, before the map.
- [Drive feedback](features/drive-feedback.md) shows over the current screen when the user comes back after a drive.
- On Android, who provides the routing shows before a drive is handed off.
