# First launch illustration

Generates the artwork behind [First launch](../../docs/features/first-launch/design.md#illustration): an aerial
view of a winding road through hills, forest, a village and a river, five screens wide, with two cars driving along
it. A day version goes with the light theme, a dusk version with the dark theme.

```sh
node tools/first-launch-art/generate.mjs          # writes everything to tools/first-launch-art/build/
node tools/first-launch-art/generate.mjs --table  # prints positions along the road, for placing the cars
```

Needs Node 18 or newer and nothing else. The scenery is seeded, so every run draws the same picture.

## Output

| File                                   | What it is                                                                 |
|----------------------------------------|----------------------------------------------------------------------------|
| `panorama-day.svg`, `panorama-dusk.svg` | The panorama without cars, 1950 × 844: five 390 × 844 screens side by side |
| `mx5-*.svg`, `gr86-*.svg`              | Each car on its own, facing right and centred on 0,0, with its shadow and dusk headlights |
| `road.json`                            | The road as a path and as cubic segments, plus where the cars sit on each step |
| `review-*.svg`                         | For checking: every step's cars at once, page edges, the area under the panel, and the cars up close |
| `OnboardingArt.dc.html`                | The illustration artboard for the design canvas                            |
| `prototype.html`                       | The interactive prototype: `prototype.template.html` with the artwork filled in |

## How it fits together

- Each step pans the panorama one full screen, so the road leaves one page exactly where it enters the next.
- The cars sit at a distance along the road (`CARS.lead`, one per step). The GR86 follows the MX-5 by `CARS.gap`
  and both keep to the right lane. Between steps, the distance is interpolated, so the cars drive as the user
  swipes.
- The cars stay in the top part of each screen, above the panel (y below about 450).

## Changing it

- **Road:** edit `NODES`. Each node is a point with a heading and two handle lengths, so the road stays smooth.
  Run `--table` afterwards and move `CARS.lead` so the cars stay on their page.
- **Colours:** the `day` and `dusk` palettes.
- **Cars:** `mx5()` and `gr86()` draw them facing +x, about 40 units long. They are simplified, with no badges or
  logos ([tone](../../docs/overview.md#tone)).

## For Compose

The SVGs can't be used as they are. Vector images don't support dashed strokes, so the road's centre line is lost,
and they don't support group opacity, which the dusk light cones use. The plan is to draw the road and the cars in
code, placed along `road.json`, and to keep the rest of the scenery as vector images split one page each. A single
bitmap five screens wide would take about 60 MB at 3× density.
