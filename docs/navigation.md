# Spirited Driving: Navigation

> **Status:** draft, being iterated. Last updated 2026-11-08.
>
> How the user moves between [features](overview.md#features).

## Structure

The [Map](features/map/README.md) is the only main screen. There is no bottom navigation: a trip happens in a sheet over
the map, and everything else opens from it.

```
First launch (once)
   ↓
Map ── sheet: Search ─► (Start search) ─► Route planning ─► Drive hand-off ─► Drive feedback
 │       │                                └─ From ─► Start search
 │       ├─ Saved routes ─► Map with the route selected
 │       └─ Recent places ─► Route planning
 └─ Profile ─┬─ Account (signed in) or sign-in (signed out)
             ├─ Trip history
             └─ Settings
```

## Map and its sheet

The map stays on screen while the user searches and plans. A sheet at the bottom shows the current step:

1. **Search.** Collapsed, the sheet shows the search field. Opened, it shows a Saved routes row with the recent
   places below it; typing replaces them with suggestions
   ([Destination search](features/destination-search/README.md)).
2. **Route planning.** Choosing a suggestion, a recent place or a point long-pressed on the map opens
   [Route planning](features/route-planning/README.md) in the sheet, with the routes drawn on the map.
   Without a usable location (precise, inside the covered area), the search for the start comes first.
   "From" in route planning opens the same search for the start
   ([design](features/destination-search/README.md#design)).
3. **One route.** A route chosen from Saved routes or Trip history shows on its own, with its details and Drive.
   There is no budget: the route isn't planned again
   ([design](features/saved-routes/design.md#a-saved-route-on-the-map)).
4. **Drive.** Starting a drive hands off to the phone's maps app ([Drive](features/drive/README.md)).
5. **After the drive.** Coming back once the drive is likely over, the sheet asks how the route was
   ([Drive feedback](features/drive-feedback/README.md)). Answering or closing it returns to search.

Back goes one step: from route planning or drive feedback to search, from the search for the start to where it
opened from, from search to the map. Back on the map leaves the app
(Android).

## Screens over the map

Saved routes and Profile open on top of the map:

- **iOS:** as sheets stacked on the map's sheet, as in Apple Maps. Trip history, Settings and Account open inside
  the Profile sheet. Swiping the sheet down or its close button returns to the map.
- **Android:** as full screens. Back returns to the map.

Choosing a route to show on the map (from Saved routes or Trip history) closes them.

## Saved routes

The Saved routes row opens [Saved routes](features/saved-routes/README.md) as a list. Choosing a route returns to the
map with that route on its own in the sheet, ready to drive.

Signed-out users see the row too, as a reminder to sign in.

## Profile

The profile button in the top right corner of the map opens Profile, signed in or not. It leads to:

- [Account](features/account/README.md), the first row when signed in. Signed out, a sign-in card takes its place.
- [Trip history](features/trip-history/README.md). Choosing a drive returns to the map with that route on its own in
  the sheet; planning new routes opens Route planning to the same destination, from where the user is now. Rating a
  past drive opens a sheet over the list and returns to it
  ([design](features/trip-history/design.md#rating-sheet)).
- [Settings](features/settings/README.md), which works the same signed in or out.

## Sign-in

Opening a feature that needs an account while signed out shows the sign-in sheet: a title saying what asked, such
as "Sign in to save this route", and the sign-in buttons ([Account design](features/account/design.md#sign-in-sheet)).
After signing in, the feature opens; closing the sheet leaves the user where they were. This covers the Saved routes
row, saving a route and Trip history.

## Prompts

- [First launch](features/first-launch/README.md) shows once, before the map.
- [Drive feedback](features/drive-feedback/README.md) is a step of the map's sheet, not a prompt over the current
  screen. If a screen over the map is open, it waits until that screen closes. Coming back later, a reminder bubble
  on the map opens it, and so does a quiet notification.
- On Android, who provides the routing shows before a drive is handed off.
