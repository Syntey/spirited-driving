# Account

> **Status:** partly built, MVP. Last updated 2026-11-17.

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

## Design

Profile and Account are in `:feature:profile`: `ProfileComponent` and `AccountComponent`, inside the Profile sheet on
iOS and full screen on Android. The code holds their layout, sizes and copy; these are the decisions behind them. Still
to build, in [design.md](design.md): Sign in with Apple, which waits for the Apple Developer Program, and opening the
sign-in sheet from Trip history.

- **The sign-in sheet** is `SignInSheetComponent`, opened over the map by Save and the Saved routes row while signed
  out. Its title says what asked, so the sign-in has a reason. After signing in it closes and the feature goes on, with
  no "signed in" message: the feature opening is the feedback. Closing it leaves the user where they were. A bottom
  sheet on Android; a native sheet on iOS, with its close button in the shared content, like the map's sheets.

- **No initials, photos or avatars,** the map's profile button included, which is always the person icon. The app is
  about the roads, not about the user.
- **Profile isn't a sign-in wall.** Signed out, its first row is a compact card with the sign-in buttons in it, so
  signing in from Profile takes one tap, and Settings works as when signed in.
- **Feedback and About are on Profile,** not in Settings, so Settings holds only preferences.
- **A Hide My Email address reads "Signed in with Apple"** on the Account row, since the relay address means nothing
  to the user. The Account screen shows it as it is: it's the address the account knows.
- **"Continue with Google",** not "Sign in", because signing in and creating an account are the same step. The button
  is drawn in Compose to Google's guidelines, its light and dark styles. Its logo is decorative to screen readers.
- **Cancelling the provider's sheet says nothing** and leaves the user where they were.
- **A failed sign-in shows above the buttons** on `errorContainer`, so the user can try again right there. When the
  email already has an account with the other provider, the message names it, so one person doesn't end up with two
  accounts and their routes split between them.
- **Signing out asks nothing:** the account keeps everything, and the button is one screen deep. Settings and recent
  places stay on the device; saved routes and trip history leave it.
- **Deleting asks first.** Google users don't sign in again: the backend deletes the Firebase user itself, which needs
  no recent sign-in, and a second account picker could hand back a different account than the one being deleted.
  A failed deletion keeps the user on Account to try again; if the backend deleted the data but not the sign-in,
  trying again finishes it.
- **Destructive actions use the theme's `error` colour** in Compose, as the theme keeps red for primary actions. The
  native iOS alert's destructive button stays system red.
- **iOS sheet colours:** the sheet on `surfaceContainerLow`; cells on `surfaceContainerLowest` in light and
  `surfaceContainerHigh` in dark.

## Open questions

1. **Deletion web page:** where it lives (next to the privacy policy) and how a request is verified.
2. **One email, both providers:** an account made with Sign in with Apple on iOS, with the email shared, can't be
   reached from Android, which has only Google. Whether to link both sign-ins to one account (Firebase account
   linking), and where the user does it.
3. **Google button font on iOS:** Google's guidelines name Roboto Medium for the label, and the app bundles no
   fonts. Check whether the system font is accepted, so both buttons match.
