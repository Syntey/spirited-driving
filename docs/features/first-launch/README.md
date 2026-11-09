# First launch

> **Status:** draft, MVP. Last updated 2026-11-07.

What a new user sees the first time they open the app.

## What the user can do

- Swipe through a short intro of three pages: what the app is for, the budget (pick a destination, say how much
  extra time or distance you'll spend, get the most fun route that fits), and the hand-off to the phone's maps app.
- Skip the intro.
- Allow location access, with an explanation of why the map needs it.
- Say whether they have a Czech motorway vignette, so routes can use motorways: Yes or No.

## Rules

- It is shown once. It counts as done when the vignette question is answered; until then it starts again from the
  beginning.
- It has an illustration of two light sports cars on a curvy road, drawn after an MX-5 and a GR86, simplified and
  without badges, following the [tone](../../overview.md#tone). It has a day version and a dusk version, for the
  light and dark themes.
- The intro says the app covers Czechia for now, and links the terms and the privacy policy.
- No sign-up: the app is ready to use right after the intro.
- The app works without location access.
- Skipping the intro still asks for location access and the vignette.
- Both vignette answers are complete; with No, routes avoid vignette motorways. The answer can be changed in
  [Settings](../settings/README.md).
- No other permission is asked here. Notifications are asked for by [Drive feedback](../drive-feedback/README.md).

## Needs and provides

- **Provides:** location permission for the [Map](../map/README.md); the user's vignette for
  [Settings](../settings/README.md).

## Account

Not needed.

## Backend

None.
