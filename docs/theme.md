# Theme

> **Status:** draft, being iterated. Last updated 2026-11-09.
>
> Colours, type, shapes, glass materials and the empty and error layout shared by every screen. Screen design builds
> on this.

## Summary

- **Racing red** `#BD162C` is the primary colour, on a cool graphite base ("asphalt") with amber accents.
- **Light and dark themes** follow the system. No dynamic colour (Material You): the app keeps its own colours.
- **System fonts:** SF Pro on iOS, Roboto on Android. No bundled fonts.
- **Glass follows the platform**, both through Haze: Liquid Glass style on iOS, frosted glass on Android.

In code, the theme is a Material 3 `MaterialTheme` in `:core:ui`, so Material components pick up the colours,
type and shapes without extra styling.

## Colour

### Roles

| Role            | Colour                 | Used for                                                                |
|-----------------|------------------------|-------------------------------------------------------------------------|
| Primary         | Racing red             | Main actions (Plan, Drive), the selected budget, focus, progress        |
| Secondary       | Asphalt (graphite)     | Secondary actions, tonal buttons, the fastest route                     |
| Tertiary        | Amber                  | How fun a route is, ratings, hints                                      |
| Error           | Burnt orange           | Failures that need action, always with an icon and text                 |
| Surface/neutral | Cool grey, asphalt hue | Backgrounds, sheets, cards, text                                        |

- Red is the only strong colour on screen. Most of the UI is neutral, so the red actions stand out.
- Error is orange, not red, so an error never looks like a primary action. Expected outcomes, such as no fun route
  within the budget, are messages, not errors.

### Values

Tonal palettes in HCT (Material Color Utilities). Light uses tone 40 for accents, dark uses tone 80, except the
dark primary, which uses tone 58 so it stays red instead of turning pink. Every text colour on its background
meets WCAG AA (4.5:1) in both themes.

| Palette         | Hue  | Chroma | Source    |
|-----------------|------|--------|-----------|
| Primary         | 19.8 | 84.4   | `#BD162C` |
| Secondary       | 250  | 12.7   | `#4E5864` |
| Tertiary        | 78.5 | 58.1   | `#F2A900` |
| Error           | 45   | 80     |           |
| Neutral         | 250  | 4      |           |
| Neutral variant | 250  | 8      |           |

**Accents**

| Role                 | Light     | Dark      |
|----------------------|-----------|-----------|
| primary              | `#BD162C` | `#FC4952` |
| onPrimary            | `#FFFFFF` | `#410007` |
| primaryContainer     | `#FFDAD8` | `#92001C` |
| onPrimaryContainer   | `#92001C` | `#FFDAD8` |
| inversePrimary       | `#FFB3B1` | `#BD162C` |
| secondary            | `#555F6B` | `#BDC7D5` |
| onSecondary          | `#FFFFFF` | `#27313C` |
| secondaryContainer   | `#D9E3F2` | `#3E4853` |
| onSecondaryContainer | `#3E4853` | `#D9E3F2` |
| tertiary             | `#7F5700` | `#FFBA3B` |
| onTertiary           | `#FFFFFF` | `#432C00` |
| tertiaryContainer    | `#FFDEAD` | `#604100` |
| onTertiaryContainer  | `#604100` | `#FFDEAD` |
| error                | `#9F4200` | `#FFB692` |
| onError              | `#FFFFFF` | `#562000` |
| errorContainer       | `#FFDBCB` | `#793000` |
| onErrorContainer     | `#793000` | `#FFDBCB` |

**Surfaces**

| Role                    | Light     | Dark      |
|-------------------------|-----------|-----------|
| background, surface     | `#F9F9FC` | `#121316` |
| onBackground, onSurface | `#1A1C1E` | `#E2E2E5` |
| surfaceVariant          | `#DEE3EB` | `#42474E` |
| onSurfaceVariant        | `#42474E` | `#C2C7CF` |
| surfaceContainerLowest  | `#FFFFFF` | `#0C0E11` |
| surfaceContainerLow     | `#F3F3F7` | `#1A1C1E` |
| surfaceContainer        | `#EEEDF1` | `#1E2022` |
| surfaceContainerHigh    | `#E8E8EB` | `#282A2D` |
| surfaceContainerHighest | `#E2E2E5` | `#333538` |
| surfaceDim              | `#DADADD` | `#121316` |
| surfaceBright           | `#F9F9FC` | `#38393C` |
| inverseSurface          | `#2F3033` | `#E2E2E5` |
| inverseOnSurface        | `#F1F0F4` | `#2F3033` |
| outline                 | `#72777F` | `#8C9199` |
| outlineVariant          | `#C2C7CF` | `#42474E` |
| scrim                   | `#000000` | `#000000` |

### Route colours

Each route keeps its colour and number on the map and in the list
([Route planning](features/route-planning/README.md)). Route colours sit outside the Material roles.

| Route         | Light     | Dark      |
|---------------|-----------|-----------|
| Fastest       | `#616C78` | `#A2ACBA` |
| Fun route 1   | `#BD162C` | `#FC4952` |
| Fun route 2   | `#6E37DD` | `#AC8AFF` |
| Fun route 3   | `#008488` | `#00BEC3` |

- The fastest route is asphalt grey: it's the reference, not the goal.
- Blue, green and yellow are left out. The base maps use them for the user's location, water, parks and major
  roads.
- Lines have a casing (white on the light map, near-black on the dark map), so they read on any road colour.
- The selected route draws on top and wider; the others are dimmed.
- Colour is never the only signal: every fun route also shows its number, and the fastest route an arrow.

## Typography

The system font on each platform, through `FontFamily.Default`: SF Pro on iOS (the Compose Multiplatform default),
Roboto on Android (or the maker's system font). Code uses the Material 3 type roles; sizes follow each platform,
so text matches the native parts around it.

| Role                | Weight   | Android (sp) | iOS text style (pt)  |
|---------------------|----------|--------------|----------------------|
| headlineLarge       | Bold     | 32 / 40      | Large Title, 34 / 41 |
| headlineMedium      | Bold     | 28 / 36      | Title 1, 28 / 34     |
| headlineSmall       | Bold     | 24 / 32      | Title 2, 22 / 28     |
| titleLarge          | SemiBold | 22 / 28      | Title 3, 20 / 25     |
| titleMedium         | SemiBold | 16 / 24      | Headline, 17 / 22    |
| titleSmall          | SemiBold | 14 / 20      | Subheadline, 15 / 20 |
| bodyLarge           | Regular  | 16 / 24      | Body, 17 / 22        |
| bodyMedium          | Regular  | 14 / 20      | Subheadline, 15 / 20 |
| bodySmall           | Regular  | 12 / 16      | Footnote, 13 / 18    |
| labelLarge          | Medium   | 14 / 20      | Callout, 16 / 21     |
| labelMedium         | Medium   | 12 / 16      | Caption 1, 12 / 16   |
| labelSmall          | Medium   | 11 / 16      | Caption 2, 11 / 13   |

Size / line height. Display roles are not used.

- **Numbers** for times, distances and budgets use tabular figures (`tnum`), so values line up in route cards and
  don't shift while they change.
- **Text size** follows the system setting on both platforms. Sheets and route cards must still work at the
  largest sizes.

## Shapes

| Shape       | Radius  | Used for                                                  |
|-------------|---------|-----------------------------------------------------------|
| Capsule     | full    | Buttons, chips, the search field, floating map buttons    |
| extraSmall  | 8 dp    | Small badges, route number markers                        |
| small       | 12 dp   | Text fields, menus                                        |
| medium      | 16 dp   | Route cards, list groups                                  |
| large       | 24 dp   | Dialogs                                                   |
| extraLarge  | 32 dp   | Sheet top corners                                         |

A shape inside another shape is concentric: its radius is the outer radius minus the padding between them. Both
Liquid Glass and Material 3 Expressive use capsules and nested rounded corners, so one set of shapes fits both
platforms.

## Glass

### Materials

|                | iOS                                        | Android                                       |
|----------------|--------------------------------------------|-----------------------------------------------|
| Look           | Liquid Glass style: refraction, blur, light | Frosted glass: blur, tint, grain              |
| Haze effect    | `hazeGlass` (`haze-glass`)                 | `hazeBlur` (`haze-blur`, `haze-blur-materials`) |
| Default style  | `GlassStyle.regular`                       | `HazeMaterials.thin()`                        |
| Over images    | `GlassStyle.clear`                         | `HazeMaterials.ultraThin()`                   |
| Tint           | `material3()`, from the surface colour     | Surface colour, from the preset               |

- One shared modifier picks the effect per platform (`.ios.kt` / `.android.kt`). Screens don't choose.
- Haze follows the light or dark theme on its own.
- Blur runs on every supported Android version (Android 12+).
- On iOS, native parts (navigation bars, alerts, native sheets) are real Liquid Glass. Haze Glass is used for
  Compose surfaces only, and not on the same edge as a native bar, so the two don't sit side by side.

### Where glass is used

Glass is for surfaces that float over moving content:

- Bars and buttons over a scrolling list, such as Saved routes and Trip history (on Android; iOS uses native bars).
- Panels and buttons over the [First launch](features/first-launch/README.md) illustration.
- Sheets and dialogs over Compose content.

Content itself (lists, cards inside a sheet, text) sits on plain surfaces.

### Over the map

Haze samples only what Compose draws, and the map is a native view, so the parts over the map don't use Haze
([Architecture](architecture.md#how-the-map-screen-is-built)):

- **iOS:** the sheet and the map buttons are native SwiftUI with Liquid Glass over the MapKit map, as in Apple
  Maps. The sheet's content is Compose on a transparent background; cards and chips inside it use plain surfaces.
- **Android:** the sheet and the map buttons are solid surfaces, as in Google Maps: the sheet on
  `surfaceContainerLow`, the buttons on `surfaceContainerHigh`.

### Accessibility

- **Reduce Transparency** (iOS): glass turns into opaque surfaces.
- **Increase Contrast** (iOS): glass gets a visible border and stronger tint.
- Haze doesn't read these settings; the app reads them on iOS and passes them to Haze
  (`LocalGlassAccessibilitySettings`).

## Empty and error states

One layout for a list or a result with nothing to show, whether it's empty or something failed:

1. **Image:** an icon in a 64 circle on `secondaryContainer`.
2. **Title:** what happened, such as "You're offline".
3. **Message:** what it means or what to do, in `onSurfaceVariant`.
4. **Try again:** a tonal button with a refresh icon, only when trying again can help. Empty states and "Not
   covered" have none.

|         | iOS                                | Android                                 |
|---------|------------------------------------|-----------------------------------------|
| Title   | Headline, 17 / 22                  | titleMedium, 16 / 24                    |
| Message | Subheadline, 15 / 20               | bodyMedium, 14 / 20                     |
| Button  | Capsule, 36 pt, 44 pt touch target | Tonal button, 40 dp, 48 dp touch target |

- It takes the place of what it stands for: the route list, the suggestions or a whole list. The header, field and
  controls above it stay, so the user can change the text or the budget right there.
- Centred, with 24 at the sides, 12 between the image and the text, and 4 between the title and the message. In a
  sheet it fills the space left; on a full screen it sits a little above the middle.
- It stays neutral. Being offline or a failure on our side isn't the user's mistake, and orange would make it look
  like one. The error colour is for an action the user took that failed, shown next to it, such as signing in
  ([Account](features/account/design.md#errors)).
- One shared Compose component. Each feature passes its icon, title, message and whether Try again shows: see
  [Destination search](features/destination-search/design.md#states),
  [Route planning](features/route-planning/design.md#states), [Saved routes](features/saved-routes/design.md#list)
  and [Trip history](features/trip-history/design.md#states).

## Native parts

The same colours reach the parts that aren't Compose:

- **iOS:** the `AccentColor` asset holds primary in light and dark, so native bars, alerts, switches and sheets
  use racing red.
- **Android:** the splash screen and system bars follow the theme; the app draws edge to edge.
- **Maps:** the map follows the app theme. MapKit switches on its own; Google Maps gets a dark JSON style (no
  map ID, see [Architecture](architecture.md#map)).

## Open questions

1. **Route colours** need checking on the real light and dark base maps of both SDKs, and with colour-blindness
   simulation.
2. **Text size:** check that Compose Multiplatform on iOS follows the Dynamic Type setting.

## Sources

- [Haze](https://chrisbanes.github.io/haze/latest/): [Glass](https://chrisbanes.github.io/haze/latest/effects/glass/),
  [Blur materials](https://chrisbanes.github.io/haze/latest/blur/materials/),
  [Blur on each platform](https://chrisbanes.github.io/haze/latest/blur/platforms/),
  [UIKit interop](https://chrisbanes.github.io/haze/latest/scenarios/uikit-interop/)
- [Haze 2.1 release](https://chrisbanes.me/posts/haze-2.1.0/)
- [Material Design 3: colour roles](https://m3.material.io/styles/color/roles)
- [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)
- [Apple HIG: Typography](https://developer.apple.com/design/human-interface-guidelines/typography)
