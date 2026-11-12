# Map

> **Status:** partly built, MVP. Last updated 2026-11-12.

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

## Design

The screen is `TripPlanComponent` in `:feature:map`. `MapScreenUi` draws it on Android and `MapComponent` on iOS, around
the sheet's shared content, `TripPlanSheetUi` ([Architecture](../../architecture.md#how-the-map-screen-is-built)). The
code holds its layout, sizes and copy; these are the decisions behind it. What isn't built yet, the half sheet's content and
what picking a point leads to, is still in [design.md](design.md).

- **The map follows the user** whenever location access is on, so it opens where they are. Panning stops it, and the
  locate button brings it back. The map doesn't turn with the heading.
- **Each platform's own controls:** the locate button sits where that platform's maps app has it, at the top right under
  the profile button on iOS and at the bottom right above the sheet on Android, with that app's symbol: an arrow on iOS,
  a target on Android. On iOS it's an SF Symbol, as Phosphor has no arrow with a slash. It's grey without location
  access.
- **The profile button is at the top right,** out of the thumb's reach, as it's used rarely.
- **Without location access** the locate button asks for it. Once the system won't ask again (iOS asks once, Android
  twice), it opens the app's page in the system settings. Access is read again each time the app comes to the front,
  so turning it on there shows at once.
- **A muted base map,** so route lines stand out: MapKit's muted emphasis, and on Android a light and a dark JSON style,
  since a map ID would move the map to a paid SKU. Business and transit points of interest are hidden on both.
- **The map's padding is the collapsed sheet's height, at every height of the sheet,** so the map's logo and legal
  notice stay visible and the camera doesn't move while the sheet is dragged. The collapsed sheet is as tall as its
  Compose content measures, so it fits larger text.
- **The covered area is kept on the device** and checked against the backend's version on each launch, so the map
  dims it at once and offline. Until the first one arrives, the map opens on Czechia's corners, undimmed.
- **Outside the covered area, the map zooms out once,** when the user's location and the covered area are first both
  known. Later locations don't move the camera, so the user can pan, and the locate button follows them as usual.
- **The dimming sits below place names on iOS,** so the towns across the border stay readable. Google Maps draws
  shapes above its labels, so on Android they're dimmed too.
- **Turn on, in the "Precise location is off" note, opens the app's page in Settings on iOS.** iOS's own prompt grants
  precise location only until the app is closed, so the note would be back on the next launch. Android's prompt
  switches it for good.
- **Once closed, the precise-location note stays closed,** kept on the device: approximate location can be the user's
  choice. Outside the covered area only that area's note shows, as it says more.
- **The sheet has three heights:** collapsed, with the notes and the search field, then half and full, which add the
  [Destination search](../destination-search/README.md#design) below the field. On Android the sheet is only as tall
  as the part that shows, so its list ends at the bottom of the screen at every height, and the locate button hides
  above collapsed, where the sheet would cover it.
- **The notes show on the collapsed sheet only,** so with the keyboard up the suggestions get the room. The collapsed
  height is measured with them, so the sheet comes back to the same height.
- **Until [Route planning](../route-planning/README.md) exists, choosing a place drops a pin** and moves the map to the
  streets around it. Touching and holding the map drops a pin there too, with a haptic tap, and the camera stays. The
  profile button does nothing yet; it opens Profile once that's built.

## Account

Not needed.

## Backend

The covered area.

## Open questions

1. **Heading on Android:** Google Maps' location dot shows the direction of travel, not the compass, so it shows no
   heading while the user stands still. A compass heading needs the app's own location marker.
2. **Zooming while following (Android):** any gesture that moves the map stops following, pinch-zoom too, while MapKit
   keeps following through a zoom. Telling a zoom from a pan on Android needs the camera's target compared with the
   user's location after the gesture.
