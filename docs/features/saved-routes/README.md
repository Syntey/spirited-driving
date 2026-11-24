# Saved routes

> **Status:** built, MVP. Last updated 2026-11-17.

Routes the user wants to drive again or later, on any device.

## What the user can do

- Save a planned route in one tap.
- See saved routes in a list, last used first.
- Drive a saved route any time, from wherever they are.
- Rename or delete a saved route.

## Rules

- A saved route keeps the exact route; it isn't planned again.
- A route is named after its destination. Names are unique: when that name is taken, the user names the route
  while saving.
- A route that uses motorways needing a vignette says so.
- Saved routes sync across the user's devices.
- Saved routes can be opened and driven offline; saving, renaming and deleting need a connection.

## Needs and provides

- **Needs:** routes from [Route planning](../route-planning/README.md); [Account](../account/README.md) for sign-in.
- **Provides:** a route for [Drive](../drive/README.md).

## Account

Needed.

## Design

The list, with rename and delete, is `SavedRoutesComponent` in `:feature:routes`: a native sheet at large height on iOS,
full screen on Android. A saved route on the map is a one-route step of the map's sheet, held by `MapSheetComponent` in
`:feature:map`, which holds Save too. The code holds their layout, sizes and copy; these
are the decisions behind them. What isn't built yet, the list's order by last use and the vignette note, is still in
[design.md](design.md).

- **Save asks nothing** when the destination's name is free: the route is named as the planning header shows it.
  A taken name opens a dialog with "Karlštejn Castle from Smíchov" (the start's area) selected, then a number after it
  when that's taken too. The dialog's Save stays disabled while the name is taken, so the user never sees a failure.
- **Names are compared ignoring case and the spaces around them,** against the user's list on the phone. A name the
  user types is up to 40 characters; a suggested name is cut to fit, before its number.
- **Saved means its route is in the list,** by the route's ID, so a route saved on another phone reads Saved too.
  Tapping Saved removes it at once, with no confirmation: the route is still on screen and can be saved again.
- **Signed out, Save and the Saved routes row open the sign-in sheet,** and the feature goes on after signing in: the
  route is saved, or the list opens ([Account](../account/README.md#design)).
- **The list is kept on the phone,** and each route's full route with it once it's saved or opened, so a saved route
  opens and is driven offline. Every sign-in, sign-out and account switch clears it before the account's list loads,
  so one user's routes never show to the next. Only the user already signed in at start keeps it until the new list
  arrives.
- **Delete hides the row at once** and is sent when the 5-second Undo ends, as a deleted route can't be saved again by
  its ID. If sending fails, the row comes back where it was and the message says why: offline, or try again.
- **Rename and delete need a connection;** offline, the message says so and nothing changes. The list itself shows
  from the phone; only a phone with nothing kept shows "You're offline" with Try again.
- **A saved route on the map isn't planned again.** It's drawn in fun route 1's colour, without a label, between a
  ring at its start and a pin at its destination. Without location access, "Starts in Smíchov." takes the place of the
  distance to the start. Save after un-saving keeps the route's name.
- **The Saved routes row is on the half sheet for signed-out users too,** as a reminder to sign in, and shows how many
  routes there are.

## Backend

List, save, rename and delete saved routes. Each saved route keeps the area name of its start and whether it uses
motorways that need a vignette.
