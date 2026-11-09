# Saved routes

> **Status:** draft, MVP. Last updated 2026-11-08.

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

## Backend

List, save, rename and delete saved routes. Each saved route keeps the area name of its start and whether it uses
motorways that need a vignette.
