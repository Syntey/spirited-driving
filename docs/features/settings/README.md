# Settings

> **Status:** built, MVP. Last updated 2026-11-15.

The user's preferences for planning, plus feedback and information about the app.

## What the user can do

- Choose distance units: km or miles.
- Mark the countries they have a valid motorway vignette for. In the MVP, that is Czechia.
- See whether drive reminders are on, and turn them on or off in the phone's settings.
- Rate the app in the App Store or Google Play.
- Send feedback from the app, anonymously.
- Read about the app: version, privacy policy, terms, OpenStreetMap attribution and open-source licences.

## Rules

- Units start from the phone's region: miles where road distances are in miles, such as the US and the UK, km
  elsewhere. Once the user picks, their choice stays.
- Units change how distances read, and the chips of a distance budget.
- There is one vignette row for each country in the covered area that has motorway vignettes, so the list grows with
  the covered area, without an app update.
- The Czech vignette is first set in [First launch](../first-launch/README.md).
- Settings stay on the device and aren't synced to the [Account](../account/README.md). First launch asks about the
  vignette on every new install, and units follow the phone's region.
- The Drive reminders switch mirrors the phone's notification settings and has no state of its own; tapping it takes
  the user there. The phone is the only place the setting lives, so the app and the phone can't disagree.
- Changes apply at once. There is no Save button.

## Needs and provides

- **Needs:** the vignette countries of the covered area, which come with the covered area the
  [Map](../map/README.md) loads; the reminder from [Drive feedback](../drive-feedback/README.md).
- **Provides:** units and vignettes for [Route planning](../route-planning/README.md); units for every screen that
  shows a distance.

## Design

The screens are in `:feature:profile`: Settings, Send feedback, About, Licences and Licence text, pushed inside the
Profile sheet on iOS and full screen on Android. The code holds their layout, sizes and copy; these are the decisions
behind them.

- **Settings holds only preferences:** Units, Motorway vignettes and Notifications. Rate, Send feedback and About are
  on [Profile](../account/design.md#profile), so Settings stays short and looks the same signed in or out.
- **The units switch** is the segmented switch from [Route planning](../route-planning/README.md#design), so km / mi
  looks like min / km. A remembered distance budget moves to the closest chip in the new unit.
- **Vignettes come with the covered area,** one row per country, with Czechia built in until the covered area has
  loaded. Changing one while a trip is planned plans it again when the user is back on the map.
- **Drive reminders mirror the phone.** Tapping the row opens the phone's notification settings for the app, or the
  "Drive reminders" channel on Android, asking for permission first where the phone still lets the app ask (iOS
  provisional authorization, the Android 13 prompt). The switch refreshes when the user comes back.
- **Rate opens the store page,** on its write-a-review action, not the in-app review prompt: the system may decide
  not to show that, and the user tapped on purpose.
- **Send feedback** shows what is sent along with the message (app version, OS, phone), so anonymous means something
  the user can check. A failed send keeps the text. Back leaves without asking.
- **Web pages open in the in-app browser,** a Safari view controller on iOS and a Custom Tab on Android, so the user
  stays in the app. Rate is the exception, since it opens the store app.
- **Licences are generated, not kept by hand.** The build lists each platform's own dependencies with
  AboutLibraries, so a new library shows up by itself and each platform lists only what it ships. The base maps
  aren't listed: Google Maps and MapKit show their own legal notices on the map. Phosphor Icons is listed by hand,
  since the app copies the icons instead of depending on the library.

## Account

Not needed.

## Backend

Receives feedback, with the app version, OS version and phone model, and no user.

## Open questions

1. **Earning money:** how the app earns money is still open. Any in-app purchase goes on Profile, not in Settings.
