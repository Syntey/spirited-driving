# Recommended drives

> **Status:** postponed until A→B routes work well; too many unknowns. Last updated 2026-11-05.

Ready-made fun round trips near the user, for when there's no destination, just the wish to drive.

## What the user can do

- See where drives start, on the map and in a list, nearest first.
- Filter by length: about 30 minutes, 1 hour or 2 hours.
- Open a drive to see its route on the map and its details.
- Drive it or save it.

## Rules

- Drives are shared by all users. The backend keeps generating new ones, so every part of the covered area has
  some.
- Everyone sees where drives start. Opening, driving and saving a drive needs sign-in.
- The list is ordered by distance until ranking by ratings is tested.

## Needs and provides

- **Needs:** the [Map](map.md) to show drives on; ratings from [Drive feedback](drive-feedback.md);
  [Account](account.md) for sign-in.
- **Provides:** a route for [Drive](drive.md) and [Saved routes](saved-routes.md).

## Account

Needed to open, drive or save a drive.

## Backend

Drives near a point, one drive's route.

## Open questions

1. **Ranking:** how ratings should order the list. Needs testing with real ratings.
2. **Details:** which numbers and details a drive shows (see
   [Route planning](route-planning.md#open-questions)).
