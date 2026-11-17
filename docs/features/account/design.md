# Account screens

> **Status:** partly built. Last updated 2026-11-16.
>
> What's left to build of [Account](README.md): Sign in with Apple, and the sign-in sheet that features needing an
> account open. Profile, the Account screen, Google sign-in, signing out and deleting are built; the decisions behind
> them are in the README's [Design](README.md#design) section. Uses the [Theme](../../theme.md).

## Sign in with Apple

Waits for the Apple Developer Program.

| Platform | Buttons, in this order                   |
|----------|------------------------------------------|
| iOS      | Continue with Apple, Continue with Google |
| Android  | Continue with Google                     |

- Same size, full-width capsules, stacked. Apple comes first: Apple requires its button to be at least as prominent
  as other sign-in buttons, and on iOS its flow is the smoother one (Face ID instead of a web page).
- Official logo and colours, drawn in Compose to Apple's guidelines: black in the light theme, white in the dark theme.
- Tapping it shows a spinner in place of its logo and disables Google, as Google's button does; then the system Apple
  sheet opens.
- **Account screen:** an Apple user who didn't share their name has no Name row.
- **Deleting:** Apple users sign in once more after the confirmation, because the app has to revoke its Sign in with
  Apple token, which needs a fresh authorization. Cancelling that cancels the deletion.

## Sign-in sheet

Opens when a signed-out user opens a feature that needs an account ([Navigation](../../navigation.md#sign-in)).

| Opened from                        | Title                             |
|------------------------------------|-----------------------------------|
| Save, in route planning            | Sign in to save this route        |
| Saved routes row, on the map       | Sign in to keep routes for later  |
| Trip history, in Profile           | Sign in to see your trip history  |

- Below the title, the same text as the Profile card, then the sign-in buttons.
- After signing in, the sheet closes and the feature goes on: the route is saved (Save reads "Saved"), or Saved
  routes or Trip history opens. No "signed in" message; the feature opening is the feedback.
- Closing the sheet leaves the user where they were.
- Errors show above the buttons, as on the Profile card ([Design](README.md#design)).

## Platforms

|                    | iOS                                                    | Android                                       |
|--------------------|--------------------------------------------------------|-----------------------------------------------|
| Sign-in sheet      | Native sheet, inset, Liquid Glass, close button        | Modal bottom sheet with scrim, `surfaceContainerLow`, top corners 32 dp |
| Apple button       | 50 pt, 17 semibold, as Google's                        | —                                             |

- The sign-in sheet keeps its close button in the Compose content, like the map's sheets.

## Copy

| Where                        | Text                                                                 |
|------------------------------|----------------------------------------------------------------------|
| Apple button                 | Continue with Apple                                                  |
| Sign-in sheet titles         | As in the table under [Sign-in sheet](#sign-in-sheet)                |
