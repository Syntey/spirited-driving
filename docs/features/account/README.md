# Account

> **Status:** draft, MVP. Last updated 2026-11-08.

Signing in, and what the user's account holds.

## What the user can do

- Sign in with Google (Android and iOS) or Apple (iOS).
- See their account: name and email from Google or Apple, and which one they signed in with.
- Sign out.
- Delete the account and all its data.

## Rules

- Everything done on this device before signing in stays with the account: drives and feedback.
- Using a feature that needs an account asks the user to sign in first, then continues with that feature.
- Signing out keeps settings and recent places on the device. Saved routes and trip history stay in the
  account.
- Deleting the account deletes everything it holds: saved routes, trip history and ratings. A dialog confirms it;
  Apple users also sign in once more, which revoking the Sign in with Apple token needs.
- Deletion is offered in the app, as both stores require, and on a web page, since Google Play also requires a link
  where users can ask for deletion without the app.

## Needs and provides

- **Provides:** sign-in for [Saved routes](../saved-routes/README.md) and [Trip history](../trip-history/README.md).

## Backend

User data keyed by the Firebase user ID (see [architecture](../../architecture.md#accounts)), attaching drives started
before sign-in, delete account (from the app and from the web page).

## Open questions

1. **Deletion web page:** where it lives (next to the privacy policy) and how a request is verified.
