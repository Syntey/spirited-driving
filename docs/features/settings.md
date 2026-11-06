# Settings

> **Status:** draft, MVP. Last updated 2026-11-06.

The user's preferences for planning, plus information about the app and a way to support it.

## What the user can do

- Choose distance units: km or miles.
- Mark the countries they have a valid vignette for. The list shows the countries with vignettes in and around the
  covered area.
- Read about the app: version, privacy policy, terms, OpenStreetMap attribution and open-source licences.
- Leave a tip to support the app, as an in-app purchase. It unlocks nothing.

## Rules

- Settings are kept on the device and synced to the [Account](account.md) once the user signs in.
- No vignettes are set until the user sets them.

## Needs and provides

- **Provides:** units and vignettes for [Route planning](route-planning.md).

## Account

Not needed.

## Backend

Settings sync, for signed-in users. Tips go through the app stores.

## Open questions

1. **Signing in on a second device:** whether the settings on the device or the ones already on the account win.
