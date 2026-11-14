# First launch illustration

Generates the artwork behind [First launch](../../docs/features/first-launch/README.md#design): an aerial
view of a winding road through hills, forest, a village and a river, five screens wide, with two cars driving along
it. A day version goes with the light theme, a dusk version with the dark theme.

```sh
node tools/first-launch-art/generate.mjs          # writes the app's scene and everything in tools/first-launch-art/build/
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

It also writes the scene for the app, `feature/onboarding/.../ui/components/art/OnboardingArtScene.kt`, which is checked in. Run the
generator after every change, so the app and the canvas show the same picture.

## How it fits together

- Each step pans the panorama one full screen, so the road leaves one page exactly where it enters the next.
- The cars sit at a distance along the road (`CARS.lead`, one per step). The GR86 follows the MX-5 by `CARS.gap`
  and both keep to the right lane. Between steps, the distance is interpolated, so the cars drive as the user
  swipes.
- The cars' centres stay between y 200 and 412 on every page, leaving the bottom of each screen to the panel. The app
  keeps that band between Skip and the panel (`ArtFit`), and `ArtFitTest` fails if the cars leave it, so after moving
  the road, update `ArtFit.CARS_TOP` and `CARS_BOTTOM` too.

## Changing it

- **Road:** edit `NODES`. Each node is a point with a heading and two handle lengths, so the road stays smooth.
  Run `--table` afterwards and move `CARS.lead` so the cars stay on their page.
- **Colours:** the `day` and `dusk` palettes.
- **Cars:** `mx5()` and `gr86()` draw them facing +x, about 40 units long. They are simplified, with no badges or
  logos ([tone](../../docs/overview.md#tone)).

## In the app

The app draws the illustration in code (`OnboardingArt` in `:feature:onboarding`), since Compose's vector images can't
draw the road's dashed centre line, and a bitmap five screens wide would take about 60 MB at 3× density.

- `OnboardingArtScene.kt` holds the scene as Kotlin draw calls, the palettes as `ArtPalette.Day` and `ArtPalette.Dusk`,
  and the road as `ArtRoad`. The generator turns the scene's SVG markup into calls to the helpers in `ArtDrawing.kt`,
  so it only knows the shapes the scene uses; a new kind of shape needs a helper and a case in the generator.
- The scenery is drawn once and moved as a layer; the cars are drawn on top, placed along `ArtRoad.segments` the
  same way the generator places them (`Road.kt`, checked by `CarsTest`).
