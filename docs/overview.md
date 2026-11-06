# Spirited Driving: Overview

> **Status:** draft, being iterated. Last updated 2026-11-06.
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
- No car makes or models are named. Illustrations show light sports cars that aren't a real model.

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

Each feature has its own file in [features/](features/). A feature is a complete piece of functionality, not a
screen. How the user moves between features is in [navigation.md](navigation.md).

MVP, for the [launch](#launch) region:

| Feature                                                | Purpose                                                  | Account          |
|--------------------------------------------------------|----------------------------------------------------------|------------------|
| [Map](features/map.md)                                 | Primary screen: a map around the user, with search       | No               |
| [Destination search](features/destination-search.md)   | Find a place by typing                                   | No               |
| [Route planning](features/route-planning.md)           | From a budget to a list of available routes              | No               |
| [Drive](features/drive.md)                             | Hand the route to Google Maps or Apple Maps              | No               |
| [Drive feedback](features/drive-feedback.md)           | Rate the route after driving it                          | No               |
| [Settings](features/settings.md)                       | Units, vignettes, tips                                   | No               |
| [Account](features/account.md)                         | Sign in with Google or Apple                             | —                |
| [First launch](features/first-launch.md)               | Intro and location permission                            | No               |
| [Saved routes](features/saved-routes.md)               | Keep routes to drive later                               | Yes              |
| [Trip history](features/trip-history.md)               | Past drives                                              | Yes              |

**Future ideas**

- Round trips: [ready-made drives nearby](features/recommended-drives.md), or on demand ("1-hour loop from here").
  Postponed until A→B routes work well; too many unknowns. First in line after that, since the audience often drives
  with no destination.
- In-app turn-by-turn navigation
- Favourite roads, roads to avoid
- Recording drives and stats
- Community-rated roads, shared routes
- Offline use
- Car profiles (a sports car and a camper van like different roads)
- Awareness of traffic and time of day

**Non-goals**

- Lap timing, speed leaderboards or anything else that rewards speeding on public roads
- Track-day tools

## Architecture

How the app is built is described in [architecture.md](architecture.md). In short:

- Route planning and place search run on a backend, a separate project behind an HTTP API. The app sends start,
  destination and budget and gets back the baseline and fun routes.
- The map is Google Maps on Android and Apple MapKit on iOS.
- Navigation in the MVP is a hand-off to Google Maps (Android) or Apple Maps (iOS) with waypoints.

## Launch

- **Region:** Czechia.
- **Language:** English.
- **Price:** free. Users can leave a tip as an in-app purchase ([Settings](features/settings.md)).

## Sources

- [ROADS by Porsche (App Store)](https://apps.apple.com/us/app/roads-by-porsche/id1438225964),
  [Porsche Newsroom](https://newsroom.porsche.com/en/2023/products/porsche-roads-by-porsche-app-update-31243.html)
- [The Long Way](https://mwm.ai/apps/the-long-way/6757197257)
- [Road Sorties](https://www.indiehackers.com/product/road-sorties)
- [Kurviger relaunch roadmap](https://forum.kurviger.com/t/app-roadmap-for-the-new-kurviger-app-fahrplan-fur-die-neue-kurviger-app/9575)
- [Garmin Adventurous Routing](https://www8.garmin.com/manuals/webhelp/zumo396/EN-US/GUID-2779F56A-1AEA-4DB2-8AB3-A58F15303128.html)
- [TomTom thrilling routes](https://developer.tomtom.com/blog/build-different/thrills-and-hills-tutorial-understanding-route-parameters-routing-api/)
