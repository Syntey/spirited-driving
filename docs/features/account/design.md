# Account screens

> **Status:** draft, being iterated. Last updated 2026-11-14.
>
> The design for [Account](README.md): Profile, the sign-in sheet, the Account screen and deleting the account.
> Profile is here because its first row is the account. Uses the [Theme](../../theme.md); where Profile opens is in
> [Navigation](../../navigation.md#profile).

No initials, photos or avatars anywhere, including the map's profile button, which is always the person icon. The
app is about the roads, not about the user.

## Profile

Opens from the profile button on the map, signed in or not.

Four groups, top to bottom. Only Feedback has a header.

| Group | Signed out                                                       | Signed in                          |
|-------|------------------------------------------------------------------|------------------------------------|
| 1     | Sign-in card                                                     | Account, with the email            |
| 2     | Trip history: "Drives from this phone show up when you sign in"; Settings: "Units, vignettes, reminders" | Trip history: the number of drives; Settings as signed out |
| 3     | Feedback: Rate Spirited Driving, Send feedback                   | The same                           |
| 4     | About Spirited Driving                                           | The same                           |

- **Sign-in card:** "Sign in", what the account keeps, and the [sign-in buttons](#sign-in-buttons) in the card
  itself, so signing in from Profile takes one tap.
- **Not a sign-in wall:** signed out, Settings works as when signed in, and the card stays compact.
- **Trip history**, signed out, opens the [sign-in sheet](#sign-in-sheet).
- **Feedback and About** are on Profile, not in Settings, so Settings holds only preferences. Their rows work as in
  [Settings design](../settings/design.md#feedback); About Spirited Driving opens
  [About](../settings/design.md#about).
- **Account row:** the second line is the email. For an Apple Hide My Email address it reads "Signed in with Apple"
  instead, since the relay address means nothing to the user.

## Sign-in buttons

| Platform | Buttons, in this order                   |
|----------|------------------------------------------|
| iOS      | Continue with Apple, Continue with Google |
| Android  | Continue with Google                     |

- Same size, full-width capsules, stacked. Apple comes first: Apple requires its button to be at least as prominent
  as other sign-in buttons, and on iOS its flow is the smoother one (Face ID instead of a web page).
- "Continue", because signing in and creating an account are the same step.
- Official logos and colours, drawn in Compose to each brand's guidelines: Apple black in the light theme and white
  in the dark theme; Google's light style (white with a grey outline) and dark style (`#131314` with a grey outline).
- Tapping one shows a spinner in place of its logo and disables the others; then the provider's own sheet opens:
  the system Apple sheet, Credential Manager's account picker on Android, a web sign-in sheet for Google on iOS.
- Cancelling the provider's sheet returns to where the user was, with no message.

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

### Errors

Shown above the buttons, on `errorContainer` with an icon, so the user can try again right there.

| Case                                              | Message                                                                     |
|---------------------------------------------------|-----------------------------------------------------------------------------|
| Offline, or the sign-in failed                    | Couldn't sign in. Check your connection and try again.                      |
| The email has an account with the other provider (iOS) | This email already has an account that uses Google. Continue with Google to sign in. (Or Apple.) |

Without the second message, one person would end up with two accounts and their routes split between them. The same
case from Android, which has no Apple button, is open question 1.

## Account

Opens from the Account row in Profile.

- **Name**, **Email** and **Signed in with** (Google or Apple, with its logo), read-only. Below them: "Your name
  and email come from your Google account." (or Apple account).
- An Apple user who didn't share their name has no Name row. A Hide My Email address shows as it is: it's the
  address the account knows.
- **Sign out**, then **Delete account** in the error colour, with "Deletes your saved routes, trip history
  and ratings for good."

## Signing out

- No confirmation: nothing is lost, since the account keeps everything, and the button is one screen deep.
- Returns to Profile, signed out.
- Settings and recent places stay on the device. Saved routes and trip history leave it.

## Deleting the account

1. **Delete account** asks first: "Delete your account?", "Your saved routes, trip history and ratings are
   deleted for good. To confirm, you'll sign in with Google once more." (or Apple), with **Delete
   account** and **Cancel**.
2. The provider's sign-in sheet opens. Firebase deletes only a user who signed in recently, and Apple requires the
   app to revoke its Sign in with Apple token, which needs a fresh authorization. It's also a deliberate second step
   before something that can't be undone. Cancelling it cancels the deletion.
3. While deleting, a spinner replaces the Delete account label and the screen takes no input.
4. Done: back to Profile, signed out, with "Account deleted". Settings and recent places stay on the device,
   as after signing out.
5. If it fails, nothing is deleted: "Couldn't delete your account. Check your connection and try again."

## Platforms

|                    | iOS                                                    | Android                                       |
|--------------------|--------------------------------------------------------|-----------------------------------------------|
| Profile            | Native sheet at large height                           | Full screen                                   |
| Title, close, back | Native navigation bar in the sheet                     | Compose top app bar, titleLarge 22 / 28, Back |
| Rows               | Inset grouped, corners 22 pt, chevrons                 | List items, 72 dp, no chevrons                |
| Sign-in card       | A grouped cell, corners 22 pt                          | `surfaceContainerHigh`, corners 16 dp         |
| Sign-in buttons    | 50 pt, 17 semibold                                     | 48 dp, Roboto Medium 14                       |
| Sign-in sheet      | Native sheet, inset, Liquid Glass, close button        | Modal bottom sheet with scrim, `surfaceContainerLow`, top corners 32 dp |
| Account            | Pushed inside the Profile sheet (native navigation)    | Full screen                                   |
| Sign out           | Row in primary                                         | List item with an icon                        |
| Delete account     | Row in error                                           | List item in error, with a trash icon         |
| Confirmation       | Native alert, Delete account in the destructive style  | Material dialog, trash icon, Cancel and Delete (Delete in error) |
| Account deleted    | Capsule on `inverseSurface` at the bottom of the sheet | Snackbar                                      |

- **Built with:** shared Compose, styled per platform as above. On iOS only the sheets, the navigation bar in the
  Profile sheet and the alert are native ([Architecture](../../architecture.md#shared-and-native-ui)). The sign-in
  sheet keeps its close button in the Compose content, like the map's sheets.
- **iOS sheet colours:** the sheet on `surfaceContainerLow`; cells on `surfaceContainerLowest` in light and
  `surfaceContainerHigh` in dark.
- **Destructive colour:** Compose surfaces use `error`, as the theme keeps red for primary actions. The native iOS
  alert's destructive button is system red, which the app doesn't change.
- Screen readers hear the sign-in buttons by their labels; the logos are decorative.

## Copy

| Where                        | Text                                                                                   |
|------------------------------|----------------------------------------------------------------------------------------|
| Profile card                 | Sign in / Your saved routes and trip history stay with your account, on any phone. Drives from this phone come along. |
| Trip history, signed out     | Drives from this phone show up when you sign in                                        |
| Trip history, signed in      | 24 drives                                                                              |
| Settings row                 | Units, vignettes, reminders                                                            |
| Account row, Hide My Email   | Signed in with Apple                                                                   |
| Buttons                      | Continue with Apple / Continue with Google                                             |
| Sign-in failed               | Couldn't sign in. Check your connection and try again.                                 |
| Other provider               | This email already has an account that uses Google. Continue with Google to sign in.   |
| Account footer               | Your name and email come from your Google account.                                     |
| Delete account row           | Deletes your saved routes, trip history and ratings for good.                          |
| Confirmation                 | Delete your account? / Your saved routes, trip history and ratings are deleted for good. To confirm, you'll sign in with Google once more. |
| Deleted                      | Account deleted                                                                        |
| Delete failed                | Couldn't delete your account. Check your connection and try again.                     |

## Open questions

1. **One email, both providers:** an account made with Sign in with Apple on iOS, with the email shared, can't be
   reached from Android, which has only Google. Whether to link both sign-ins to one account (Firebase account
   linking), and where the user does it.
2. **Google button font on iOS:** Google's guidelines name Roboto Medium for the label, and the app bundles no
   fonts. Check whether the system font is accepted, so both buttons match.
