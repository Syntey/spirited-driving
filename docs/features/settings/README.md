# Settings

> **Status:** draft, MVP. Last updated 2026-11-13.

The user's preferences for planning, plus information about the app. Its screens are in [design.md](design.md).

## What the user can do

- Choose distance units: km or miles.
- Mark the countries they have a valid motorway vignette for. In the MVP, that is Czechia.
- See whether drive reminders are on, and open the phone's settings to change that.
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
- Drive reminders have no switch of their own in the app. The phone's notification settings are the only switch, so
  the app and the phone can't disagree.
- Changes apply at once. There is no Save button.

## Needs and provides

- **Needs:** the vignette countries of the covered area, which come with the covered area the
  [Map](../map/README.md) loads; the reminder from [Drive feedback](../drive-feedback/README.md).
- **Provides:** units and vignettes for [Route planning](../route-planning/README.md); units for every screen that
  shows a distance.

## Account

Not needed.

## Backend

Receives feedback, with the app version, OS version and phone model, and no user.

## Open questions

1. **Earning money:** how the app earns money is still open. Any in-app purchase goes in Settings.
