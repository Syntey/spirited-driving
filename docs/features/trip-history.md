# Trip history

> **Status:** draft, MVP. Last updated 2026-11-06.

Every drive the user started from the app, newest first.

## What the user can do

- See past drives: date, start, destination and the rating given.
- Drive the same route again, or plan again from the same start and destination with a new budget.
- Rate a drive that wasn't rated yet.
- Delete a drive from the history, or clear it.

## Rules

- Drives started on this device before signing in show up once the user signs in.
- History syncs across the user's devices.

## Needs and provides

- **Needs:** recorded drives from [Drive](drive.md); ratings from [Drive feedback](drive-feedback.md);
  [Account](account.md) for sign-in.
- **Provides:** a route for [Drive](drive.md); a start and destination for [Route planning](route-planning.md);
  unrated drives for [Drive feedback](drive-feedback.md).

## Account

Needed.

## Backend

List and delete recorded drives.
