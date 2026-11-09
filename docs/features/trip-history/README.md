# Trip history

> **Status:** draft, MVP. Last updated 2026-11-08.

Every drive the user started from the app, newest first. Its main use is finding a past drive to drive it again or
keep it.

## What the user can do

- See past drives: date, start, destination, distance and the rating given.
- Drive the same route again, or save it to [Saved routes](../saved-routes/README.md).
- Plan new routes to the same destination.
- Rate a drive from the last week that wasn't rated yet.
- Delete a drive, or clear the whole history.

## Rules

- Driving a past route again uses the exact route; it isn't planned again.
- Planning again starts where the user is now. A start the user picked by hand stays.
- Only drives on a fun route can be rated, as [Drive feedback](../drive-feedback/README.md) asks about no others.
  A route rated on an earlier drive shows that rating on later drives.
- Rating is offered for 7 days after a drive. Older drives that weren't rated show no rating.
- Deleting a drive deletes its rating too, and clearing the history deletes all of them.
- Drives started on this device before signing in show up once the user signs in.
- History syncs across the user's devices.
- History can be opened and its drives driven offline; saving, deleting and clearing need a connection. A rating
  made offline is sent later.
- A drive marked as not driven in [Drive feedback](../drive-feedback/README.md) doesn't show.

## Needs and provides

- **Needs:** recorded drives from [Drive](../drive/README.md); ratings from [Drive
  feedback](../drive-feedback/README.md); [Account](../account/README.md) for sign-in.
- **Provides:** a route for [Drive](../drive/README.md) and [Saved routes](../saved-routes/README.md); a destination
  and a start picked by hand for [Route planning](../route-planning/README.md); ratings of past drives for [Drive
  feedback](../drive-feedback/README.md).

## Account

Needed.

## Backend

List, delete and clear recorded drives. Each drive keeps its route, the area name of its start, whether the start
was the user's location, whether it was the fastest route, whether its route is saved, and its rating. Deleting a
drive deletes its rating.

## Open questions

1. **How long drives are kept:** until the user deletes them, or for a set time such as 12 months. Trip history is
   a record of where the user has been.
