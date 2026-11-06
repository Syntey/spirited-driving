# Route planning

> **Status:** draft, MVP. Last updated 2026-11-06.

The core of the app: from a start, a destination and a detour budget to a list of available routes.

## What the user can do

- Plan from their location to a destination, or change the start to any place.
- Pick the budget from a row of chips, in minutes (+15 min to +2 h) or km, and switch between the two.
- See the fastest route and 1–3 fun routes together, on the map and as a list.
- Compare the routes. Each shows its extra time over the fastest route, its total time and distance, how fun it is,
  and one or two facts about its roads, such as km of curvy roads or metres climbed.
- Select a route in the list or on the map; the selection shows in both.
- Drive the selected route or save it.

## Rules

- Changing the start, the destination or the budget plans again.
- Budgets are absolute: minutes or km on top of the fastest route, not a percentage of it.
- Each route has its own colour and number, the same on the map and in the list.
- How fun a route is uses one scale for every route, the fastest one included, so routes can be compared.
- Nothing shown is about speed.
- The first budget is a fixed default in time; the user can't change the default. After that, the budget
  remembers the last type and value used.
- All routes, the fastest one included, use only motorways the user has a vignette for. Until the user sets their
  vignettes, routes avoid vignette motorways and a hint points to the setting.
- Clear messages cover the cases where no fun route fits the budget, the area isn't covered, or the phone is
  offline.

## Needs and provides

- **Needs:** places from [Destination search](destination-search.md) or the [Map](map.md); units and
  vignettes from [Settings](settings.md).
- **Provides:** the selected route for [Drive](drive.md) and [Saved routes](saved-routes.md); route lines drawn on
  the [Map](map.md).

## Account

Not needed.

## Backend

Plan a trip.

## Open questions

1. **Chip values:** the steps between +15 min and +2 h, and the km chips. Needs real data.
2. **Route facts:** which facts describe a route best. Curvy-road distance and climbing come first; check them in
   testing.
