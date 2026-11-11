# First launch

> **Status:** built, MVP. Last updated 2026-11-10.

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

## Design

The screen is `OnboardingScreenUi` in `:feature:onboarding`. The code holds its layout, sizes and copy; these are the
decisions behind it.

- **Five steps:** what the app is, the budget, the hand-off, Location, Vignette. Every page swipes both ways. Swiping on
  from Location works like its Continue: until the system prompt is answered, the page springs back and the prompt
  shows, so a swipe never skips it. Whatever the answer, the vignette follows. Back on Android goes to the previous page
  and leaves the app from the first; on iOS, which has no system back, swiping is the way back.
- **Page dots for the intro only,** fading out with Skip on the way to Location, so Location and Vignette read as
  questions after the intro.
- **One page per decision:** Location has a single Continue, since the system prompt has its own way to decline. The
  vignette's Yes and No are both primary buttons, like every other step's, so neither answer looks preferred: a Yes
  without a vignette risks a fine, while a wrong No only skips the motorway.
- **Terms line on the first page,** so Skip passes it too.
- **The panel keeps one height,** set by the tallest page, so it doesn't jump while swiping; with large text it grows,
  and its pages scroll once it reaches the top of the screen. It's on the default glass, not the clear one the theme
  has for images, because it holds text.
- **The illustration slides along the road** with the pages, a panorama five screens wide, so the road leaves each
  page where it enters the next and the cars drive on as the user swipes. This turns the five steps into one drive.
  With Reduce Motion (iOS) or animations off (Android) it fades from step to step instead. It fills the screen's
  height. Where the panel is taller than in the design, on a short screen or with large text, it moves up so the cars
  stay above the panel, and shrinks only when moving further would take them under Skip.
- **The illustration is drawn in code.** [tools/first-launch-art](../../../tools/first-launch-art/README.md)
  generates it, as SVG for the design canvas and as Kotlin draw calls for the app, from one scene. Vector images
  can't draw the road's dashed centre line, and a bitmap five screens wide would take about 60 MB.
- **Copy names the platform's maps app and location prompt:** Apple Maps and "Keep Precise Location on" on iOS,
  Google Maps and "Choose Precise" on Android.
- The "Czechia for now" label is on `tertiaryContainer`, as the theme uses tertiary for hints. The budget chips are
  the ones from Route planning, not tappable.

## Account

Not needed.

## Backend

None.

## Open questions

1. **Artwork:** whether the generated illustration stays, or someone draws a final one. The app can draw any artwork
   the generator turns into its scene.
2. **Terms and privacy policy:** where the pages are published. Until then the links in the terms line open
   placeholders (`LegalPages` in `:core:resources`).
3. **Wide screens (known issue):** on tablets, unfolded foldables and phones in landscape, the illustration shows
   bare land beside the first and last pages, as the scenery ends just past them, and the panel spans the whole
   width. In landscape on a phone the panel also covers the cars. Fixing it needs wider scenery from the generator
   and a maximum width for the panel.
