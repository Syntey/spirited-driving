# Route planning

> **Status:** partly built, MVP. Last updated 2026-11-13.

The core of the app: from a start, a destination and a detour budget to a list of available routes.

## What the user can do

- Plan from their location to a destination, or change the start to any place.
- Pick the budget from a row of chips, in minutes (+15 min to +2 h) or distance (+10 to +100 km, or +5 to +60 mi), and switch between the two.
- See the fastest route and 1–3 fun routes together, on the map and as a list.
- Compare the routes. Each shows its extra time over the fastest route, its total time and distance, how fun it is,
  and one or two facts about its roads, such as km of curvy roads or metres climbed.
- Select a route in the list or on the map; the selection shows in both.
- Drive the selected route or save it.

## Rules

- Changing the start, the destination or the budget plans again.
- Budgets are absolute: minutes, km or miles on top of the fastest route, not a percentage of it.
- Each route has its own colour and number, the same on the map and in the list.
- How fun a route is uses one scale for every route, the fastest one included, so routes can be compared.
- Nothing shown is about speed.
- The first budget is a fixed default in time; the user can't change the default. After that, the budget
  remembers the last type and value used.
- All routes, the fastest one included, use only motorways the user has a vignette for.
- Clear messages cover the cases where no fun route fits the budget, the area isn't covered, or the phone is
  offline.
- When no fun route fits, the plan says how much budget the closest fun route needs, and the user can pick the
  smallest budget that fits in one tap.

## Needs and provides

- **Needs:** places from [Destination search](../destination-search/README.md) or the [Map](../map/README.md); units and
  vignettes from [Settings](../settings/README.md).
- **Provides:** the selected route for [Drive](../drive/README.md) and [Saved routes](../saved-routes/README.md);
  route lines drawn on the [Map](../map/README.md).

## Design

Route planning is a step of the map's sheet: `TripPlanComponent` in `:feature:map` holds it, `TripPlanSheetUi` draws
the sheet on both platforms, and each platform's map draws the routes. The code holds its layout and copy; these are
the decisions behind it. What isn't built yet, Save, is still in [design.md](design.md). Drive is in
[Drive](../drive/README.md#design).

- **Cancel, not an X, closes it,** back to the search as it was: an X on a sheet reads as dismissing it, but the map's
  sheet stays and only steps back.
- **The trip starts from the user's location only when it's precise and in the covered area.** An approximate start
  can be kilometres off, and the maps app would guide the user there. Without a usable location, choosing a
  destination opens the search for the start first ([Destination search](../destination-search/README.md#design)).
  Planning from the user's location waits for the first location, as right after access is given.
- **"From" in the header changes the start.** A start picked by hand is a ring on the map, apart from the
  destination's pin and the user's location dot, and the camera frames it instead of the user.
- **Fun routes come first, most fun first; the fastest comes last** as the reference the extras are measured from.
  Route 1 is selected when the plan arrives, or the fastest when no fun route fits.
- **One fun scale for every route, the fastest included,** as five rising bars, so routes can be compared.
  Screen readers hear "Fun 4 of 5".
- **The extra is in the budget's unit,** "+27 min" or "+26 km", in the list and on the line's label.
- **The selected route draws on top and wider, the others dimmed;** tapping a row, a line or its label selects it.
  Each label sits at the middle of its line.
- **The camera fits all routes above the sheet,** and again once the sheet settles after a drag. The profile and
  locate buttons hide while planning, as the routes take the map.
- **Picking a chip or switching the unit plans again at once.** The choice is kept on the device, with the last value
  in each unit, so the next trip starts with it; before the first pick it's +30 min.
- **Distance chips follow the distance unit in [Settings](../settings/README.md):** miles have chips of their own
  (+5, 10, 20, 30, 50, 60 mi), not converted km. A kept distance budget moves to the closest chip in the other unit.
- **When no fun route fits,** the fastest route is listed under a message naming what the closest fun route needs, with
  a button for the smallest chip that fits it, in the same unit. Without one big enough the button is left out.
- **Not covered, offline and failed take the list's place;** the header and budget stay, so a new budget plans again.
  A plan the backend can't route shows as failed.
- **The collapsed sheet is measured off-screen,** so moving between the search and planning never makes it jump.
- **Numbers use tabular figures,** so the rows' times and distances line up.

## Account

Not needed.

## Backend

Plan a trip.

## Open questions

1. **Chip values:** the steps between +15 min and +2 h, the km chips (10–100) and the mile chips (5–60). Needs real data.
2. **Route facts:** which facts describe a route best. Curvy-road distance and climbing come first; check them in
   testing.
3. **Fun scale:** five steps read at a glance. Check with real plans whether routes often land on the same step.
4. **Labels on the map** can overlap on short trips. Check their placement on real routes.
