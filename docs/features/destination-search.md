# Destination search

> **Status:** draft, MVP. Last updated 2026-11-05.

Finding a place by typing. Its field is part of the [Map](map.md), and [Route planning](route-planning.md) uses the
same search to change the start.

## What the user can do

- See suggestions while typing, with nearby places first.
- See recent destinations when the field is empty.
- Choose a suggestion as the destination or the start.

## Rules

- Recent destinations are kept on the device.
- A point picked on the map gets the nearest address as its label.

## Needs and provides

- **Provides:** a place (name, address, coordinates) for [Route planning](route-planning.md), as the destination or
  the start.

## Account

Not needed.

## Backend

Search places, reverse lookup.
