# Drive feedback

> **Status:** draft, MVP. Last updated 2026-11-06.

A quick question after a drive: how was the route?

## What the user can do

- Rate the route with a thumbs up or down.
- Add optional reasons, such as "great roads", "too many towns", "rough surface" or "took too long".
- Skip it.
- Rate a past drive later.

## Rules

- The app asks when the user comes back to it after handing off a drive, once at least half of the route's duration
  has passed. Coming back sooner doesn't count as the end of the drive.
- The question comes at most once per drive.
- Ratings go to the backend, where they help improve routes.
- Rating a past drive later happens in [Trip history](trip-history.md), so it needs an account.

## Needs and provides

- **Needs:** a recorded drive from [Drive](drive.md); past drives from [Trip history](trip-history.md).
- **Provides:** ratings for [Trip history](trip-history.md).

## Account

Not needed, except to rate a past drive later.

## Backend

Rate a drive.

## Open questions

Both depend on the implementation.

1. **When to stop asking:** someone who opens the app days after a drive shouldn't be asked about it. How long the
   question stays open is still to be decided.
2. **Few answers:** people rarely open the app again after arriving, so expect few ratings. How to get more is still
   to be decided.
