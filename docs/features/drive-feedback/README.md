# Drive feedback

> **Status:** draft, MVP. Last updated 2026-11-08.

A quick question after a drive: how was the route?

## What the user can do

- Rate the route with a thumbs up or down.
- Add optional reasons, a different set for each: what stood out, such as "curvy roads" or "nice views", or what
  didn't work, such as "too narrow", "rough surface" or "maps app went another way".
- Say they didn't drive it after all.
- Skip it.
- Get a quiet reminder to rate the route after arriving.
- Rate a drive from the last week later.

## Rules

- Only drives on a fun route are asked about. A drive on the fastest route isn't.
- A route the user has rated before isn't asked about again, such as a saved route driven often.
- The app asks when the user comes back to it after handing off a drive, once at least half of the route's duration
  has passed. Coming back sooner doesn't count as the end of the drive.
- Coming back soon after the drive, the user gets the question. Coming back later, they get a reminder on the map
  instead, which they can dismiss.
- A notification reminds the user at about the planned arrival time. It is quiet: no sound and no pop-up, so it
  can't distract while driving.
- The question comes at most once per drive. Answering, skipping or dismissing it in one place ends it everywhere.
- A new drive replaces an open question; the older drive can still be rated in
  [Trip history](../trip-history/README.md).
- A drive the user didn't drive is removed from [Trip history](../trip-history/README.md).
- A rating made offline is sent once the phone is back online.
- Ratings go to the backend, where they help improve routes.
- Rating a past drive later happens in [Trip history](../trip-history/README.md), so it needs an account. It's
  offered for 7 days after the drive; a rating from weeks later says little about the route.

## Needs and provides

- **Needs:** a recorded drive from [Drive](../drive/README.md); past drives from [Trip
  history](../trip-history/README.md).
- **Provides:** ratings for [Trip history](../trip-history/README.md).

## Account

Not needed, except to rate a past drive later.

## Backend

Rate a drive, and mark a drive as not driven.

## Open questions

Both depend on the implementation.

1. **When to stop asking:** how long after the planned arrival the question still opens on its own before the
   reminder takes over, and how long the reminder lasts.
2. **Answer rate:** check after launch how many drives get rated, and whether the notification and the reminder are
   enough.
