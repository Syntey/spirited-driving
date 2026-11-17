# Drive feedback

> **Status:** built, MVP. Last updated 2026-11-16.

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

## Design

The rating step and the reminder bubble are part of the map's screen, held by `TripPlanComponent` in `:feature:map`;
the notification is the drive reminders binding in `:core:platform`. The code holds their layout, sizes and copy;
these are the decisions behind them.

- **The phone keeps one open question,** about the newest drive, with what the step shows: the route's line, number,
  destination and planned time and distance. So the question works after a restart and offline. A new drive replaces
  it, and only a drive on a fun route gets one.
- **When it asks:** nothing before half the planned duration has passed. Then the rating step opens on its own until
  2 hours after the planned arrival; later, the reminder bubble shows instead, until 48 hours after the hand-off, when
  the question is dropped. Two hours covers stops on the way; after that the user is more likely opening the app for a
  new trip, and a sheet in the way annoys. 48 hours covers "yesterday"; a later rating says little.
- **A trip planned after the hand-off isn't thrown away:** the step only takes the place of the planning it was handed
  off from. Otherwise the question waits as the bubble.
- **The first thumb answers the question everywhere,** and each change of thumb or reason is sent at once, replacing
  the one before. Closing the step without a thumb, or dismissing the bubble, skips it. A rating made offline, and a
  drive marked as not driven, are kept on the drive and sent with the pending drives on the next start of the map,
  after the drive itself is recorded.
- **"I didn't drive it"** drops a drive the backend never got; a recorded one is removed there, and isn't claimed at
  sign-in meanwhile.
- **A route rated before isn't asked about again,** checked against the drives kept on this phone, as only a saved
  route can be driven twice.
- **No fun meter in the step,** so the planned score doesn't sway the answer.
- **The reasons** are about the roads first, then the trip. "Too narrow" is about cars: single-track lanes are fun on
  a motorbike and tiring in a car. "Heavy traffic" and "Roadworks" aren't the route's fault, and having them keeps
  those drives out of "Took too long". "Maps app went another way" means the maps app left the planned roads, so the
  rating isn't held against the route ([Architecture, test cases](../../architecture.md#test-cases)). No free text.
- **Picked thumbs and chips are amber,** as the theme gives ratings to tertiary; red stays for Done.
- **The bubble shows only on the collapsed search step,** and the map's padding grows by its height while it shows,
  so the map's logo and legal notice stay visible. iOS draws it as native glass, like the map buttons.
- **The notification asks at the hand-off plus 1.2 times the planned duration,** as the planned arrival leaves out
  stops and the way to a start elsewhere. It never lights up the screen, pops up or makes a sound: provisional
  authorization and the passive level on iOS, so there's no permission prompt; the low-importance "Drive reminders"
  channel on Android. Android schedules it as background work, which keeps it across a reboot and may deliver it some
  minutes late, later while the phone sleeps; it's a nudge, not an alarm, so it needs no exact-alarm permission. It
  isn't shown while the app is in front.
- **The reminder offer** shows on Android 13 and later only, after the first thumb, once, whatever the answer, and not
  when notifications are allowed already.

## Open questions

1. **Answer rate:** check after launch how many drives get rated, and whether the notification and the bubble are
   enough.
