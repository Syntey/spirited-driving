# Spirited Driving: Overview

> **Status:** draft, being iterated. Last updated 2026-11-10.
>
> Entry point for product and architecture design. Topics that outgrow this file get their own file in `docs/`
> and a link from here.

## The idea

A navigation app for people who enjoy driving. You pick a destination like in any navigation app, and you also say
how much extra you are willing to spend on the trip, for example **+30 minutes** or **+30 km**. The app then
picks the most enjoyable route that fits within that allowance. It swaps boring highways for curvy country
roads, hills and passes.

> **Example:** the mall is 30 km away by highway (about 20 min). With a budget of +30 min, the app finds the best
> mix of roads that gets you there in at most about 50 min.

## Spirited driving

Spirited driving is an engaging style of driving where a person enjoys their vehicle's handling, acceleration, and
engine sounds while generally staying within safe/controlled boundaries. It differs from a standard daily commute by
focusing on driver involvement rather than just basic transportation.

## Who it's for

Drivers of light, affordable sports cars. These cars aren't fast, but they are fun on curvy roads at legal speeds.
Their owners often go out with no destination, just to drive, and meet in clubs and online communities. Anyone who
enjoys driving can use the app.

## Tone

How the app talks to users, in the app and in the store listing:

- Words describe the road and the drive (curvy, scenic, enjoyable), never speed. The app shows no speeds.
- No car makes or models are named. Illustrations may draw well-known light sports cars, simplified and without
  badges or logos.

## Competitive landscape

Researched 2026-11-03. Details come from app-store listings, press releases and forums; check them hands-on.

| App                                                  | Focus             | How you control "fun"                                                    |
|------------------------------------------------------|-------------------|--------------------------------------------------------------------------|
| ROADS by Porsche                                     | Cars, free        | Style: curvy / balanced / dynamic; A→B or round trip                     |
| The Long Way (iOS, Jan 2026)                         | Cars              | Weights curvature, elevation, POIs; hands off to Apple/Google Maps       |
| Road Sorties (web, iOS)                              | Cars              | "Scenic" or "Curvy" mode; Valhalla routing on OpenStreetMap              |
| Kurviger, Calimoto, Scenic, Curv, REVER, MyRoute-app | Motorcycles first | Curviness level, e.g. fastest → extra curvy                              |
| Garmin Adventurous Routing, TomTom "thrilling"       | Devices / API     | Curviness and hilliness level                                            |
| Google Maps, Waze, Apple Maps                        | Everyone          | Only "avoid highways"                                                    |

Gaps we target:

1. **A budget instead of a curviness level.** Other apps ask "how curvy?" and show the cost afterwards. We ask "how
   much extra?" and find the most fun route within that limit.
2. **Everyday A→B trips**, not only planned weekend rides.
3. **Cars first, light sports cars in particular.** The mature tools are built for motorcycles.

Risk: a budget control is easy to copy. The lasting advantage has to be the quality of the fun score and a
navigation experience good enough for daily use.

## Core concepts

- **Baseline route**: the fastest route from A to B, computed by *our* engine. The budget is measured against this
  route, so both times come from the same speed model.
- **Detour budget**: how much longer than the baseline the route may be, either in time (+min) or distance (+km).
  A route is valid when `time ≤ baseline time + budget` (or the same for distance).
- **Fun score**: how enjoyable a road is to drive.
- **Fun route**: the valid route with the highest fun score.

"Fun" means engaging roads, not speed. Travel times assume legal speeds.

## Features

Each feature has its own folder in [features/](features/): `README.md` says what it does, and `design.md` how its
screens look (see [Design](#design)). A feature is a complete piece of functionality, not a screen. How the user
moves between features is in [navigation.md](navigation.md).

MVP, for the [launch](#launch) region:

| Feature                                                     | Purpose                                             | Account |
|-------------------------------------------------------------|-----------------------------------------------------|---------|
| [Map](features/map/README.md)                               | Primary screen: a map around the user, with search  | No      |
| [Destination search](features/destination-search/README.md) | Find a place by typing                              | No      |
| [Route planning](features/route-planning/README.md)         | From a budget to a list of available routes         | No      |
| [Drive](features/drive/README.md)                           | Hand the route to Google Maps or Apple Maps         | No      |
| [Drive feedback](features/drive-feedback/README.md)         | Rate the route after driving it                     | No      |
| [Settings](features/settings/README.md)                     | Units, vignettes, about the app                     | No      |
| [Account](features/account/README.md)                       | Sign in with Google or Apple                        | —       |
| [First launch](features/first-launch/README.md)             | Intro, location permission, vignette                | No      |
| [Saved routes](features/saved-routes/README.md)             | Keep routes to drive later                          | Yes     |
| [Trip history](features/trip-history/README.md)             | Past drives                                         | Yes     |

**Future ideas**

- Round trips: [ready-made drives nearby](features/recommended-drives/README.md), or on demand ("1-hour loop from
  here"). Postponed until A→B routes work well; too many unknowns. First in line after that, since the audience
  often drives with no destination.
- In-app turn-by-turn navigation
- Choice of maps app for the hand-off, such as Google Maps on iOS or Mapy.com. Depends on how each one takes
  waypoints, and how it works with Android Auto and CarPlay.
- Favourite places, such as Home and Work
- Favourite roads, roads to avoid
- Recording drives and stats
- Community-rated roads, shared routes
- Offline use
- Car profiles (a sports car and a camper van like different roads)
- Awareness of traffic and time of day

**Non-goals**

- Lap timing, speed leaderboards or anything else that rewards speeding on public roads
- Track-day tools

## Design

- **[Theme](theme.md):** colours, type, shapes, glass and the empty and error layout, shared by every screen.
- **Screens:** each feature's `design.md`, so far [Map](features/map/design.md),
  [Destination search](features/destination-search/design.md), [Route planning](features/route-planning/design.md), [Drive](features/drive/design.md),
  [Drive feedback](features/drive-feedback/design.md),
  [Account](features/account/design.md), [Saved routes](features/saved-routes/design.md),
  [Settings](features/settings/design.md) and [Trip history](features/trip-history/design.md). A design's Platforms table says how the one shared screen is styled on
  each platform; it never means two screens ([Architecture](architecture.md#shared-and-native-ui)). Built so far:
  [First launch](features/first-launch/README.md#design), and the [Map](features/map/README.md#design) without its sheet
  heights and picking a point.

Written design is a spec for building a screen, not a record of it. Once a screen is built, the code holds its
layout, sizes and copy:

- Its `design.md` is deleted. The decisions the code can't explain, and why they were made, move to a short
  "Design" section in the feature's `README.md`.
- Screenshots come from screenshot tests and are never added by hand, so they stay current.
- The theme keeps its reasons and drops its tables of values.

## Architecture

How the app is built is described in [architecture.md](architecture.md). In short:

- Route planning and place search run on a backend, a separate project behind an HTTP API. The app sends start,
  destination and budget and gets back the baseline and fun routes.
- The map is Google Maps on Android and Apple MapKit on iOS.
- Navigation in the MVP is a hand-off to Google Maps (Android) or Apple Maps (iOS) with waypoints.

## Launch

- **Region:** Czechia.
- **Language:** English.
- **Price:** free. How the app earns money is open ([Settings](features/settings/README.md#open-questions)).

## Sources

- [ROADS by Porsche (App Store)](https://apps.apple.com/us/app/roads-by-porsche/id1438225964),
  [Porsche Newsroom](https://newsroom.porsche.com/en/2023/products/porsche-roads-by-porsche-app-update-31243.html)
- [The Long Way](https://mwm.ai/apps/the-long-way/6757197257)
- [Road Sorties](https://www.indiehackers.com/product/road-sorties)
- [Kurviger relaunch roadmap](https://forum.kurviger.com/t/app-roadmap-for-the-new-kurviger-app-fahrplan-fur-die-neue-kurviger-app/9575)
- [Garmin Adventurous Routing](https://www8.garmin.com/manuals/webhelp/zumo396/EN-US/GUID-2779F56A-1AEA-4DB2-8AB3-A58F15303128.html)
- [TomTom thrilling routes](https://developer.tomtom.com/blog/build-different/thrills-and-hills-tutorial-understanding-route-parameters-routing-api/)
