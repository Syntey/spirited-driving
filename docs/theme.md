# Theme

> **Status:** colours, type, shapes, glass, buttons, chips, segmented switch, rating, progress, icon buttons, menus, sheet header, the empty and
> error layout, notes, messages, lists, route badge, fun meter, placeholder rows and the app icon built. Last updated 2026-11-11.
>
> Colours, type, shapes, glass materials and the empty and error layout shared by every screen. Screen design builds
> on this.

## Summary

- **Racing red** `#BD162C` is the primary colour, on a cool graphite base ("asphalt") with amber accents.
- **Light and dark themes** follow the system. No dynamic colour (Material You): the app keeps its own colours.
- **System fonts:** SF Pro on iOS, Roboto on Android. No bundled fonts.
- **Glass follows the platform**, both through Haze: Liquid Glass style on iOS, frosted glass on Android.

In code, the theme is `AppTheme` in `:core:ui`, a Material 3 `MaterialTheme`, so Material components pick up the
colours, type and shapes without extra styling. The values are in `core/ui/.../theme/` (`Colors.kt`, `Typography.kt`,
`Shapes.kt`); this page keeps the reasons.

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

Tonal palettes in HCT (Material Color Utilities), from racing red `#BD162C`, asphalt `#4E5864`, amber `#F2A900`, a
burnt orange error and a cool grey neutral with the asphalt hue. Light uses tone 40 for accents, dark uses tone 80,
except the dark primary, which uses tone 58 so it stays red instead of turning pink. Every text colour on its
background meets WCAG AA (4.5:1) in both themes.

### Route colours

Each route keeps its colour and number on the map and in the list
([Route planning](features/route-planning/README.md)). Route colours sit outside the Material roles, in
`AppTheme.customColors`: `routeFastest`, then `routeFun1` to `routeFun3` in red, violet and teal.

- The fastest route is asphalt grey: it's the reference, not the goal.
- Blue, green and yellow are left out. The base maps use them for the user's location, water, parks and major
  roads.
- Lines have a casing (white on the light map, near-black on the dark map), so they read on any road colour.
- The selected route draws on top and wider; the others are dimmed.
- Colour is never the only signal: every fun route also shows its number, and the fastest route an arrow.

## Typography

The system font on each platform, through `FontFamily.Default`: SF Pro on iOS (the Compose Multiplatform default),
Roboto on Android (or the maker's system font). Code uses the Material 3 type roles; sizes follow each platform,
so text matches the native parts around it:

- **Android:** the Material 3 type scale, with headlines in Bold and titles in SemiBold.
- **iOS:** each role at the size of the iOS text style it stands for, such as Large Title for `headlineLarge`, Body
  for `bodyLarge` and Footnote for `bodySmall`, without Material's letter spacing, since SF Pro spaces its own.
- Display roles are not used.
- **Numbers** for times, distances and budgets use tabular figures (`withTabularFigures()`), so values line up in
  route cards and don't shift while they change.
- **Text size** follows the system setting on both platforms. Sheets and route cards must still work at the
  largest sizes.

## Icons

Icons are SVG files in `:core:resources` (`MR.images`): the ones the app uses, from [Phosphor](https://phosphoricons.com)
(MIT, see `core/resources/PHOSPHOR-LICENSE.txt`), in the Regular weight and Fill for a selected state, named by meaning.
`:core:ui` uses only Compose: its components take icons (`Painter`) and texts as parameters, so it doesn't depend on
`:core:resources` and a new string or icon doesn't rebuild them. The more button draws its own dots, ⋮ on Android and ⋯ on
iOS. Directional icons, such as chevrons and Back, flip in right-to-left languages with `mirrorIn`.

## Shapes

Capsules for buttons, chips, the search field and floating map buttons. The Material shapes, from small to large:
small badges and route number markers, text fields and menus, route cards and list groups, dialogs, and sheet top
corners.

A shape inside another shape is concentric: its radius is the outer radius minus the padding between them. Both
Liquid Glass and Material 3 Expressive use capsules and nested rounded corners, so one set of shapes fits both
platforms.

## Glass

### Materials

`GlassSurface` in `:core:ui` is the one shared glass. It picks the effect per platform (`.ios.kt` / `.android.kt`), so
screens don't choose:

- **iOS:** Liquid Glass style with refraction, blur and light, through Haze Glass (`GlassStyle.regular`), tinted
  from the surface colour (`material3()`).
- **Android:** frosted glass with blur, tint and grain, through Haze blur (`HazeMaterials.thin()`), tinted from the
  surface colour.
- Haze follows the light or dark theme on its own. Blur runs on every supported Android version (Android 12+).
- Over images, a clearer glass (`GlassStyle.clear`, `HazeMaterials.ultraThin()`) suits a small control. A panel that
  holds text, like the one in [First launch](features/first-launch/README.md#design), keeps the default.
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
- Haze doesn't read these settings; `AppTheme` reads them on iOS and passes them to Haze
  (`LocalGlassAccessibilitySettings`).
- **Reduce Motion** (iOS) and animations turned off (Android): `AppTheme.reduceMotion`. Things that slide across the
  screen fade instead.

## Buttons

`AppButton` in `:core:ui` is every text button, with a colour and a size:

- **Colours:** primary for a screen's main action, tonal for the others, tertiary for accepting a hint such as Turn on,
  text for a quiet action, and error for a destructive one in a Material dialog.
- **Sizes:** large for the actions of a screen or sheet, medium for Try again, small for an action in a list row such as
  Rate. Even a small button has a 44 pt / 48 dp touch target.
- Labels wrap instead of truncating, so the button grows with larger text.
- **Loading:** the [spinner](#progress) takes the icon's place, or the label's when there's no icon. The button keeps its
  width and colours, and ignores taps.

## Chips

`AppChip`: unselected, outlined on Android and with a tinted fill on iOS. Selected, a choice out of a row, such as a
budget, is filled with primary; any number out of a set, such as the reasons after a rating, is on
`tertiaryContainer` with a check. Screen readers hear a choice as a radio button and a set as checkboxes.

## Segmented switch

`SegmentedSwitch`: a choice of two or three, such as km / mi in [Settings](features/settings/design.md#units) and
min / km in [Route planning](features/route-planning/design.md#budget). An outlined segmented button on Android; on iOS
a segmented control whose selected segment is a raised pill that slides, or jumps with Reduce Motion. Screen readers
hear each segment by its full name, such as Kilometres.

## Rating

`RatingThumbs`: Good and Not good, two equal 56 pt / 56 dp buttons, unselected like a chip. The picked one is on
`tertiaryContainer` with a filled thumb: amber, since the theme gives ratings to tertiary, and red stays for the main
action.

## Route badge and fun meter

- `RouteBadge`: a route's number on its [route colour](#route-colours), or an arrow for the fastest route, on the small
  rounded square the shapes keep for route markers. Text is white on the darker light-theme colours and near-black on
  the lighter dark-theme ones.
- `FunMeter`: five rising bars in amber with "Fun" below them, the empty ones faint so the scale shows. Screen readers
  hear "Fun 4 of 5".

## Progress

`CarSpinner`: a car seen from above, spinning like a drift, each turn springing into the next. It's the app's own, the
same on both platforms, in buttons and wherever something loads. With Reduce Motion or animations off it pulses
instead. Screen readers hear it as in progress.

## Icon buttons

- **Close** ends a sheet or a step in it. On iOS it's a small filled circle, as in iOS sheets; on Android a plain icon
  button.
- **More** opens a menu: ⋯ on iOS, ⋮ on Android. The menu has 12 corners on `surfaceContainer`, with icons before the
  labels on Android and after them on iOS; a destructive item, such as Delete, is in the error colour.
- Both have a 44 pt / 48 dp touch target, however small the circle.

## Sheet header

`SheetHeader` tops every sheet: a title, an optional line below it, and Close. The title is Title 2 on iOS and
titleLarge on Android, and wraps rather than truncating. A tappable line, such as "From My location" in
[Route planning](features/route-planning/design.md#layout), gets a chevron and stays grey, since red is kept for main
actions. Close sits at the top, level with the title's first line.

## Notes

`NoteCard`: a note inside a sheet or screen, with an optional icon, title, action and Close. A hint, such as "Precise
location is off" on the [Map](features/map/design.md#states), is on `tertiaryContainer` with a tertiary action. A
failed action the user can retry right there, such as signing in ([Account](features/account/design.md#errors)), is
on `errorContainer`, and screen readers announce it. Corners follow the platform's cards: 22 pt on iOS, 16 dp on
Android.

## Messages

`MessageHost` shows a short message at the bottom: a capsule on `inverseSurface` on iOS, a snackbar on Android, with
an action such as Undo in `inversePrimary`. A message shows for 4 seconds, or 5 with an action, so there's time to
reach it. Screen readers read it when it appears.

## Lists

`ListRow` and `ListGroup` in `:core:ui`, one component styled per platform:

- **iOS:** inset grouped, as in iOS Settings: rows on a card with 22 pt corners, inset 16 pt from the edges, with
  hairline separators that start at the text. Rows are 44 pt, a value sits after the label, and rows that open a
  screen end in a chevron. The card is white in light and `surfaceContainerHigh` in dark; over the map's glass sheet
  it's translucent.
- **Android:** Material list items on the sheet, 56 dp or 72 dp with a second line, with the value as that line and
  no chevrons. Group headers in titleSmall.
- Leading icons sit in a circle on `secondaryContainer`: 30 pt on iOS, 40 dp on Android.
- A dimmed row, such as a place outside the covered area, stays tappable. A loading row shows the
  [spinner](#progress) in place of its label.
- **Swipe left** deletes a row, the same on both platforms, revealing red behind it. Screen readers get a Delete action
  instead. A row the screen keeps, such as when offline, slides back.
- **Placeholder rows** stand in for a list that's loading: a circle and two bars in a faint grey, pulsing softly once a
  second, and still with Reduce Motion. Screen readers hear "Loading" once for the list.

## Empty and error states

`StateMessage` in `:core:ui`.

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

## App icon

The [spinner's](#progress) car in a bend, on racing red: an asphalt road with a white centre line, and the car in
white.

- The icon and the spinner are the same car, so the app has one mark wherever you see it.
- The background is racing red, the one strong colour, as on screen. In the dark appearance on iOS it turns graphite
  and the car takes the dark theme's red.
- **iOS:** an Icon Composer document (`AppIcon.icon`) with the road, centre line and car as layers, so the system adds
  the Liquid Glass light and makes the tinted and clear appearances itself.
- **Android:** an adaptive icon, with a monochrome layer for themed icons in which the car is cut out of the road.
- Both are generated from one drawing by [tools/app-icon](../tools/app-icon/README.md).

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
