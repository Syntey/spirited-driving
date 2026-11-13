# Trip history screens

> **Status:** draft, being iterated. Last updated 2026-11-09.
>
> The design for [Trip history](README.md): the list, rating a past drive, deleting and clearing, and a past drive
> on the map. Uses the [Theme](../../theme.md); the list opens from [Profile](../account/design.md#profile)
> ([Navigation](../../navigation.md#profile)). It looks and works like [Saved routes](../saved-routes/design.md).

## List

Opens from the Trip history row in Profile. Signed out, that row opens the
[sign-in sheet](../account/design.md#sign-in-sheet) instead.

- **Order:** newest first, by when the drive was handed off, under a header for each month: "November" in the
  current year, "October 2025" before it.
- **Each row:**
    - the destination's name
    - "Thu 5 Nov · From Smíchov · 58 km": the day, the start's area and the distance. "Today" and "Yesterday" for
      the last two days. A drive on the fastest route adds "Fastest": "Mon 2 Nov · From Karlín · Fastest · 41 km".
      Only the start's area truncates.
    - the rating, a **Rate** button or nothing ([below](#rating))
    - a more button with **Plan new routes** and **Delete**
- Tapping a row closes Profile and shows the route on the map ([below](#a-past-drive-on-the-map)).
- **Swipe left** on a row deletes it, on both platforms.
- The list loads more drives as the user scrolls, with placeholder rows at the end while they load.

## Rating

What the row shows at its end:

| The drive                                                 | Shows                               |
|-----------------------------------------------------------|-------------------------------------|
| Rated, or its route was rated on an earlier drive         | The thumb, filled, in tertiary      |
| On a fun route, not rated, from the last 7 days           | **Rate**                            |
| Not rated and older, or on the fastest route              | Nothing                             |

The thumb isn't a button: a rating can't be changed from the list.

### Rating sheet

**Rate** opens a sheet over the list with the content of Drive feedback's
[rating step](../drive-feedback/design.md#rating-step), so the user stays in the list and can rate several drives
in a row:

1. **Header:** "How was the route to Křivoklát Castle?" and a close button. Below it, "Yesterday, 16:40 · 1 h 20 ·
   74 km". No route badge: there's no map to match it to.
2. **Thumbs:** "Good" and "Not good".
3. **After a thumb is picked:** the thanks line, the reasons for that thumb, and **Done**.

- Behaves like the rating step: tapping a thumb saves the rating at once, the other thumb switches it, and the
  reasons picked are kept however the sheet closes.
- Left out: "I didn't drive it", since Delete does that here; the attribution, since no route is shown; the
  [reminder offer](../drive-feedback/design.md#reminder-offer-android), which belongs after a drive.
- Done, the close button, swiping down and Back (Android) close the sheet. With a thumb picked, the row shows it in
  place of Rate. Without one, nothing changes.
- Rating here ends the drive's question everywhere: the rating step, the reminder bubble and the notification
  ([Drive feedback](../drive-feedback/README.md#rules)).
- The sheet fits its content and grows when the reasons appear.

## A past drive on the map

The one-route step of the map's sheet, as for a [saved route](../saved-routes/design.md#a-saved-route-on-the-map):
the route on its own, the same map, notes, sheet heights and attribution, with these differences:

- **Header:** the destination's name, with "Thu 5 Nov, 14:20 · From Smíchov" below it.
- **Save** (tonal) or **Saved**, and **Drive**. Save works as in [Route planning](../route-planning/design.md#save):
  the route is saved under the destination's name, and when that name is taken the user names it first
  ([Saved routes](../saved-routes/design.md#saving)). Tapping Saved removes it from saved routes.
- Driving it records a new drive, which goes to the top of the history.

Close and Back (Android) go to the search step.

## Plan new routes

- **Plan new routes** in the row's menu closes Profile and opens [Route planning](../route-planning/design.md) to
  the drive's destination, with the budget last used.
- **Start:** the user's location when the drive started there; otherwise the start the user picked by hand. Without
  a usable location, the search for the start comes first, as for any plan
  ([Navigation](../../navigation.md#map-and-its-sheet)).

## Delete

- **Delete** in the menu, or swiping left, removes the drive at once and shows "Drive deleted" with **Undo** for
  5 seconds. Undo puts it back in its place. No confirmation, as in Saved routes.
- When Undo is gone, the drive and its rating are deleted for good.
- Deleting the drive that [Drive feedback](../drive-feedback/design.md) is still asking about also removes the
  reminder bubble and the notification.

## Clear

- A more button in the title bar holds **Clear trip history**, in the error colour. It's hidden while the history
  is empty.
- It asks first: "Clear trip history?", "All 24 drives and their ratings are deleted for good.", with **Cancel**
  and **Clear**. No Undo after that.
- Cleared, the list shows the empty state.

## States

- **Loading:** placeholder rows.
- **Empty:** the [empty and error layout](../../theme.md#empty-and-error-states) with a clock with a backwards
  arrow, "No drives yet" and "Routes you drive show up here, so you can drive them again."
- **Offline:** the list comes from the device, and its drives can be opened and driven. A rating is kept and sent
  once the phone is online. Delete and Clear show "You're offline. Changes need a connection."; Save shows "You're
  offline. Saving needs a connection.", as in Saved routes. Plan new routes opens Route planning, which shows its
  own offline message.
- **Offline, nothing on the device** (a phone the user just signed in on): the same layout with "You're offline"
  and "Trip history needs a connection.", with Try again.

## Platforms

|                       | iOS                                                          | Android                                          |
|-----------------------|--------------------------------------------------------------|--------------------------------------------------|
| List                  | Pushed inside the Profile sheet (native navigation), inset grouped rows, corners 22 pt | Full screen, list items 72 dp |
| Title, back           | Native navigation bar                                        | Compose top app bar, titleLarge 22 / 28, Back    |
| Month headers         | Section headers, as in iOS lists                             | titleSmall, `onSurfaceVariant`                   |
| Rating                | Filled thumb, tertiary, 20 pt                                | Filled thumb, tertiary, 20 dp                    |
| Rate                  | Capsule on `secondaryContainer`, 32 pt, 44 pt touch target   | Tonal button, 32 dp, 48 dp touch target          |
| More button           | ⋯, 44 pt                                                     | ⋮, 48 dp                                         |
| Menu                  | Corners 12 pt, icons after the labels                        | `surfaceContainer`, corners 12 dp, icons before the labels |
| Clear                 | ⋯ in the navigation bar, native menu                         | ⋮ in the top app bar                             |
| Rating sheet          | Native sheet over the Profile sheet, Liquid Glass, fitted to its content, close button in the Compose content | Modal bottom sheet with scrim, `surfaceContainerLow`, top corners 32 dp |
| Deleted               | Capsule on `inverseSurface`, Undo in `inversePrimary`        | Snackbar, Undo in `inversePrimary`               |
| Clear confirmation    | Native alert, Clear in the destructive style                 | Material dialog, trash icon, Cancel and Clear (Clear in error) |
| Past drive on the map | As a saved route                                             | As a saved route                                 |

- **Built with:** shared Compose, styled per platform as above; the rows, the menu, swiping and the rating content
  are the same Compose code on both. On iOS only the navigation bar and its menu, the rating sheet's container and
  the alert are native ([Architecture](../../architecture.md#shared-and-native-ui)).
- **Colours:** as in [Account](../account/design.md#platforms): the iOS sheet on `surfaceContainerLow`, cells on
  `surfaceContainerLowest` in light and `surfaceContainerHigh` in dark. Delete is in the error colour in the menu.
- **Screen readers** hear a row as one item, "Karlštejn Castle, Thursday 5 November, from Smíchov, 58 km, rated
  Good", the Rate button as "Rate the drive to Křivoklát Castle", and the more button as "More for Karlštejn
  Castle".

## Copy

| Where                  | Text                                                                 |
|------------------------|----------------------------------------------------------------------|
| Title                  | Trip history                                                         |
| Month header           | November / October 2025                                              |
| Row                    | Thu 5 Nov · From Smíchov · 58 km (or Today, Yesterday)               |
| Row, fastest route     | Mon 2 Nov · From Karlín · Fastest · 41 km                            |
| Rate button            | Rate                                                                 |
| Menu                   | Plan new routes / Delete                                             |
| Rating sheet           | How was the route to Křivoklát Castle? / Yesterday, 16:40 · 1 h 20 · 74 km |
| Past drive header      | Thu 5 Nov, 14:20 · From Smíchov                                      |
| Deleted                | Drive deleted / Undo                                                 |
| Clear                  | Clear trip history                                                   |
| Clear confirmation     | Clear trip history? / All 24 drives and their ratings are deleted for good. / Cancel · Clear |
| Empty                  | No drives yet / Routes you drive show up here, so you can drive them again. |
| Offline, changes       | You're offline. Changes need a connection.                           |
| Offline, saving        | You're offline. Saving needs a connection.                           |
| Offline, nothing yet   | You're offline / Trip history needs a connection. / Try again        |

The rating sheet's thumbs, reasons and Done use Drive feedback's [copy](../drive-feedback/design.md#copy).

## Open questions

1. **One route, many drives:** a saved route driven every day fills the history with the same row. Whether to
   group repeats, once real use shows how common they are.
