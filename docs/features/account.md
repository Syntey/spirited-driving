# Account

> **Status:** draft, MVP. Last updated 2026-11-06.

Signing in, and what the user's account holds.

## What the user can do

- Sign in with Google (Android and iOS) or Apple (iOS).
- See their profile: name and email from Google or Apple.
- Sign out.
- Delete the account and all its data.

## Rules

- Everything done on this device before signing in stays with the account: drives, feedback and settings.
- Using a feature that needs an account asks the user to sign in first, then continues with that feature.

## Needs and provides

- **Provides:** sign-in for [Saved routes](saved-routes.md) and [Trip history](trip-history.md); settings sync for
  [Settings](settings.md).

## Backend

User data keyed by the Firebase user ID (see [architecture](../architecture.md#accounts)), attaching drives started
before sign-in, delete account.
