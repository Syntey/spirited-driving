# Map

> **Status:** draft, MVP. Last updated 2026-11-07.

The primary screen, inspired by Waze: a full-screen map around the user, with the destination search on it.

## What the user can do

- See their position and heading on a map that follows them. Panning stops following; recentering resumes it.
- Search for a destination in the search field on the map ([Destination search](../destination-search/README.md)).
- Long-press the map to pick that point as the destination.
- See which area the app covers.

## Rules

- Without location permission, the map opens on the covered area and everything else works. The start of a trip is
  then picked by hand.
- With approximate location only, the map shows the rough area, but the start of a trip is still picked by hand: an
  approximate start can be kilometres off, and the maps app would guide the user there. A note offers to turn on
  precise location.
- Picking a destination outside the covered area explains that it isn't covered yet.

## Needs and provides

- **Needs:** [Destination search](../destination-search/README.md) for the search field.
- **Provides:** a picked point as a place for [Route planning](../route-planning/README.md), and a map that
  [Route planning](../route-planning/README.md) draws route lines on.

## Account

Not needed.

## Backend

None of its own.
