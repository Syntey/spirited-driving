# Account screens

> **Status:** partly built. Last updated 2026-11-17.
>
> What's left to build of [Account](README.md): Sign in with Apple.
> Profile, the Account screen, Google sign-in, the sign-in sheet, signing out and deleting are built; the decisions
> behind them are in the README's [Design](README.md#design) section. Uses the [Theme](../../theme.md).

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


## Platforms

|                    | iOS                                                    | Android                                       |
|--------------------|--------------------------------------------------------|-----------------------------------------------|
| Apple button       | 50 pt, 17 semibold, as Google's                        | —                                             |

## Copy

| Where                        | Text                                                                 |
|------------------------------|----------------------------------------------------------------------|
| Apple button                 | Continue with Apple                                                  |
