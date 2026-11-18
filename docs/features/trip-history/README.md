# Trip history

> **Status:** built, MVP. Last updated 2026-11-18.

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
- Planning again starts where the user is now, wherever the drive started.
- Only drives on a fun route can be rated, as [Drive feedback](../drive-feedback/README.md) asks about no others.
  Each drive shows only its own rating, so a route driven again can be rated again from here.
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

## Design

The list is `TripHistoryComponent` in `:feature:routes`, pushed inside the Profile sheet on iOS and full screen on
Android. A past drive on the map is the map's one-route step, as for a [saved route](../saved-routes/README.md#design),
held by `TripPlanComponent` in `:feature:map`. The code holds their layout, sizes and copy; these are the decisions
behind them.

- **Signed out, the Trip history row opens the sign-in sheet over Profile,** and the list opens after signing in
  ([Account](../account/README.md#design)).
- **Drives are grouped by month, newest first,** by when they were handed off. Months of the current year show
  without the year.
- **A row's end says what's left to do:** the drive's own thumb, **Rate** for a fun drive from the last 7 days, or
  nothing. The thumb isn't a button, so a rating isn't changed from the list.
- **Rate opens a sheet over the list,** with the rating step's thumbs and reasons, so several drives can be rated in a
  row. Each tap saves the rating. It leaves out "I didn't drive it", since Delete does that here, and the route badge,
  the attribution and the reminder offer, which belong to the map after a drive.
- **Rating here ends the drive's question everywhere,** and deleting the drive that
  [Drive feedback](../drive-feedback/README.md) is asking about removes its reminder bubble and notification.
- **The list is kept on the phone as far as it was loaded,** with the full route of each drive opened and of every
  drive started on this phone, so past drives open and are driven offline. Account changes clear it, as for saved
  routes. A rating made offline is kept and sent the next time the list loads.
- **The list loads 30 drives at a time** as the user scrolls, with placeholder rows while they load.
- **Delete hides the row at once** and is sent when the 5-second Undo ends, as in Saved routes. **Clear asks first**
  and has no Undo. Its message doesn't count the drives, as the phone knows only the ones loaded.
- **A past drive on the map isn't planned again.** Save names it after its destination, as in route planning.
- **Plan new routes plans from where the user is now,** whatever the drive's start, with the budget last used.
- **The details line truncates as a whole,** as every list row does, not only the start's area.
- **Dates follow the app's language,** with the phone's region when the phone is in that language, so "Thu 5 Nov" may read "Thu, Nov 5".

## Account

Needed.

## Backend

List, delete and clear recorded drives. Each drive keeps its route, with its start and whether it's the fastest
route, and its rating. Deleting a drive deletes its rating. Whether a drive's route is saved comes from
[Saved routes](../saved-routes/README.md) on the phone.

## Open questions

1. **How long drives are kept:** until the user deletes them, or for a set time such as 12 months. Trip history is
   a record of where the user has been.
2. **One route, many drives:** a saved route driven every day fills the history with the same row. Whether to group
   repeats, once real use shows how common they are.
