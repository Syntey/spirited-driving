# Settings screens

> **Status:** draft, being iterated. Last updated 2026-11-14.
>
> The design for [Settings](README.md): the Settings screen, the Feedback rows, About, Licences and Send feedback.
> Uses the [Theme](../../theme.md). Settings, the Feedback rows and About are on
> [Profile](../account/design.md#profile) ([Navigation](../../navigation.md#profile)).

## Settings

Only the user's preferences, in three groups, top to bottom. It looks and works the same signed in or out. Feedback
and About are on Profile.

| Group              | Rows                                      | Footer                                                        |
|--------------------|-------------------------------------------|---------------------------------------------------------------|
| Units              | Distance: km / mi                         |                                                               |
| Motorway vignettes | One switch per country: Czechia           | Without a vignette, routes avoid motorways that need one.     |
| Notifications      | Drive reminders, with a switch            | A quiet notification after a drive, asking how the route was. |

### Units

- **Distance** has a km / mi switch in the row, the same component as the min / km switch in
  [Route planning](../route-planning/README.md#design). Screen readers hear "Kilometres" and "Miles".
- At the largest text sizes, the switch moves below the label.
- Until the user picks, the selected unit follows the phone's region ([README](README.md#rules)).
- The unit changes every distance in the app: search results, route summaries, saved routes, trip history and the
  distance budget. A remembered distance budget moves to the closest chip in the new unit.

### Motorway vignettes

- One row per country, named by the country ("Czechia"), with a switch. The whole row toggles it.
- The countries come with the covered area. Czechia is built in, for when the covered area hasn't loaded yet.
- Changing a vignette while a trip is planned in the map's sheet plans it again when the user is back on the map.

### Drive reminders

The row's switch shows whether the phone delivers the
[drive feedback notification](../drive-feedback/design.md#notification); it mirrors the phone and has no state of
its own. Tapping the row or the switch opens the phone's settings for it, or asks for permission first, as below. The
switch refreshes when the user comes back to the app.

| Platform        | On when                                                  | Tapping opens                                                     |
|-----------------|----------------------------------------------------------|-------------------------------------------------------------------|
| iOS             | Authorized, provisionally or fully                       | The app's notification settings; asks for provisional authorization first if the app hasn't yet, which shows no prompt |
| Android 12 and earlier | App notifications and the "Drive reminders" channel are on | The "Drive reminders" channel's settings                 |
| Android 13 and later   | The same, and the notification permission is granted      | Without the permission: the system prompt while Android still shows it, the app's notification settings after that. With it: the channel's settings |

## Feedback

The Feedback group on [Profile](../account/design.md#profile).

- **Rate Spirited Driving** opens the app's page in the App Store, on its write-a-review action, or in Google Play.
  It isn't the in-app review prompt: the system may decide not to show that, and the user tapped on purpose.
- **Send feedback** opens [Send feedback](#send-feedback).

## About

Opens from About Spirited Driving on [Profile](../account/design.md#profile). One group: Privacy policy, Terms,
Licences, Version.

- **Privacy policy** and **Terms** open the same web pages as [First launch](../first-launch/README.md), in the
  in-app browser.
- **Licences** opens [Licences](#licences).
- **Version:** "1.0.0 (42)", the version and the build number. The row isn't tappable.

## Licences

1. **Route data and place search:** "© OpenStreetMap contributors", "Available under the Open Database License.",
   and a link to openstreetmap.org/copyright.
2. **Open-source libraries:** every library the app uses, by name, generated at build time from the app's
   dependencies (AboutLibraries). Tapping one opens its licence text.
3. **Icons:** "Phosphor Icons", with its MIT licence. Listed by hand, since the app copies the icons it uses instead of
   depending on the library.

The base maps aren't listed: Google Maps and Apple MapKit show their own legal notices on the map.

## Send feedback

Feedback goes to the backend, so it needs no mail app and stays in one place with the rest of the app's data. It's
anonymous: no email and no account, even when signed in, so there's nothing to reply to.

- A multi-line field, "What's on your mind?", that grows with the text, up to 2,000 characters. Below it, what is
  sent along: "Sent with: Spirited Driving 1.0.0 (42) · iOS 26.1 · iPhone 17 Pro".
- **Send** below, the large primary button, disabled while the field is blank. While sending, a spinner replaces
  its label and the field takes no input.
- **Sent:** the form gives way to "Thanks for your feedback" and "Every message is read.", with **Done**, which
  returns to Profile.
- **Not sent:** the text stays and a message above Send says why: "You're offline. Send it again once you're
  back online." or "Couldn't send your feedback. Try again."
- Back while the field has text leaves without asking; the text is gone.

## Platforms

|                    | iOS                                                    | Android                                         |
|--------------------|--------------------------------------------------------|-------------------------------------------------|
| Settings           | Pushed inside the Profile sheet (native navigation)    | Full screen                                     |
| Title, back        | Native navigation bar                                  | Compose top app bar, titleLarge 22 / 28, Back   |
| Groups             | Inset grouped, corners 22 pt, header and footer text as in iOS Settings | A card per row, 2 dp apart, group corners 20 dp and 4 dp between rows; headers in titleSmall, `onSurfaceVariant`; footers in bodySmall |
| Rows               | 44 pt, the value after the label, chevrons on rows that open a screen | List items 56 dp, or 72 dp with the value as supporting text; no chevrons |
| Units switch       | Pill segmented switch, the iOS control's size          | Pill segmented switch, 40 dp, labelLarge        |
| Vignette and reminder switches | iOS-style switch in primary                | Material 3 switch                               |
| Web links          | Arrow pointing up and right after the label            | Open-in-new icon after the label                |
| Browser            | Safari view controller                                 | Custom Tab                                      |
| Send feedback      | Pushed inside the Profile sheet                        | Full screen                                     |
| About              | Pushed inside the Profile sheet                        | Full screen                                     |
| Licences           | Pushed inside the Profile sheet                        | Full screen                                     |
| Licence text       | Pushed, plain text                                     | Full screen, plain text                         |

- **Built with:** shared Compose, styled per platform as above. On iOS only the navigation bar and the Safari view
  controller are native ([Architecture](../../architecture.md#shared-and-native-ui)).
- **Colours:** as in [Account](../account/design.md#platforms): the iOS sheet on `surfaceContainerLow`, cells on
  `surfaceContainerLowest` in light and `surfaceContainerHigh` in dark.

## Copy

| Where                  | Text                                                                  |
|------------------------|-----------------------------------------------------------------------|
| Title                  | Settings                                                              |
| Units                  | Units / Distance / km · mi                                            |
| Vignettes              | Motorway vignettes / Czechia                                          |
| Vignettes footer       | Without a vignette, routes avoid motorways that need one.             |
| Notifications          | Notifications / Drive reminders                                       |
| Notifications footer   | A quiet notification after a drive, asking how the route was.         |
| Feedback (Profile)     | Feedback / Rate Spirited Driving / Send feedback                      |
| About row (Profile)    | About Spirited Driving                                                |
| Send feedback          | Send feedback / What's on your mind? / Sent with: Spirited Driving 1.0.0 (42) · iOS 26.1 · iPhone 17 Pro / Send |
| Feedback sent          | Thanks for your feedback / Every message is read. / Done              |
| Feedback not sent      | You're offline. Send it again once you're back online. / Couldn't send your feedback. Try again. |
| About                  | About / Privacy policy / Terms / Licences / Version                   |
| Licences, route data   | Route data and place search / © OpenStreetMap contributors / Available under the Open Database License. |
| Licences, libraries    | Open-source libraries                                                 |
