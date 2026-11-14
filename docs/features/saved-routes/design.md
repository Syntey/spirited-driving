# Saved routes screens

> **Status:** draft, being iterated. Last updated 2026-11-09.
>
> The design for [Saved routes](README.md): saving a route, the list, renaming and deleting, and a saved route on
> the map, the one-route step of the map's sheet that [Trip history](../trip-history/README.md) uses too. Uses the
> [Theme](../../theme.md); the map and its sheet are described in the [Map screen](../map/design.md).

## Saving

- **Save** in [Route planning](../route-planning/design.md) saves the selected route at once, named after the
  destination as the planning header shows it ("Karlštejn Castle"). Nothing is asked.
- Once saved, the button reads **Saved** with a filled bookmark. Tapping it again removes the route from saved
  routes, with no confirmation: the route is still on screen and can be saved again.
- **Name already used:** names are unique. When a saved route already has the destination's name, a dialog asks for
  a name before saving: "Name this route", "You already have a route called Karlštejn Castle.", a field with
  "Karlštejn Castle from Smíchov" (the start's area) selected, and **Cancel** and **Save**. If that name is taken
  too, a number follows it: "Karlštejn Castle from Smíchov 2". Cancel doesn't save.
- **Signed out:** the [sign-in sheet](../account/design.md#sign-in-sheet) comes first; after signing in, the route
  is saved.
- **Offline:** "You're offline. Saving needs a connection.", in the same message capsule or snackbar as below.

## List

Opens from the Saved routes row on the map ([Navigation](../../navigation.md#saved-routes)).

- **Order:** last used first, saved or driven, whichever is later, so routes driven often stay on top.
- **Each row:**
    - the name
    - "From Smíchov · 1 h 05 · 58 km": the start's area, the total time and the distance
    - the fun meter, as in the [route list](../route-planning/README.md#design)
    - a more button with **Rename** and **Delete**
- The extra over the fastest route isn't shown: it was measured from that start, on that day.
- Tapping a row closes the list and shows the route on the map ([below](#a-saved-route-on-the-map)).
- **Swipe left** on a row deletes it, on both platforms.
- **Empty:** the [empty and error layout](../../theme.md#empty-and-error-states) with a bookmark, "No saved routes
  yet" and "Save a route while planning to drive it again later."
- **Offline:** the list comes from the device and its routes can be driven. Rename and Delete show "You're offline.
  Changes need a connection."
- **Offline, nothing on the device** (a phone the user just signed in on): the same layout with "You're offline"
  and "Saved routes need a connection.", with Try again.

## Rename

- A dialog: "Rename route", a field with the current name selected, the keyboard up, and **Cancel** and **Save**.
- Save is disabled while the field is empty or unchanged. A name another route already has shows "You already have
  a route with this name." under the field and disables Save.
- Names are up to 40 characters.

## Delete

- **Delete** in the menu, or swiping left, removes the route at once and shows "Route deleted" with **Undo** for
  5 seconds. Undo puts it back in its place. No confirmation: Undo costs less and covers the mistakes that matter.

## A saved route on the map

The one-route step of the map's sheet ([Navigation](../../navigation.md#map-and-its-sheet)). There is no budget:
the route isn't planned again.

**Map**

- The route in the colour of fun route 1, drawn as selected, without a label.
- A ring marks the start, a pin the destination; the user's location dot shows where they are.
- The camera fits the route and the user into the part of the map above the sheet.
- The profile and locate buttons are hidden, as while planning.

**Sheet**, top to bottom:

1. **Header:** the name, the start's area ("From Smíchov") and a close button. The start can't be changed.
2. **Summary:** the total time and distance, the facts line and the fun meter.
3. **Notes**, when they apply:
    - "Starts 12 km from you. Apple Maps takes you there first." (Google Maps on Android), when the start is more
      than about 1 km from the user ([Drive](../drive/README.md#design)). Without location
      access: "Starts in Smíchov."
    - "Needs a motorway vignette", when the route uses motorways that need one, whatever Settings say.
4. **Saved** (tonal) and **Drive** (filled, wider). Tapping Saved removes the route from saved routes; the sheet
   stays, and the button reads Save again.
5. **Attribution:** "Route data © OpenStreetMap contributors". On Android, "Routing provided by Spirited Driving"
   comes first ([Drive](../drive/README.md#design)).

Close and Back (Android) go to the search step.

| Height        | Shows                                                       |
|---------------|-------------------------------------------------------------|
| Half, default | Everything above                                            |
| Collapsed     | The name, the summary, Saved and Drive, and the attribution |

## Platforms

|                     | iOS                                                       | Android                                          |
|---------------------|-----------------------------------------------------------|--------------------------------------------------|
| List                | Native sheet at large height, inset grouped rows, corners 22 pt | Full screen, list items 72 dp              |
| More button         | ⋯, 44 pt                                                  | ⋮, 48 dp                                         |
| Menu                | Corners 12 pt, icons after the labels                     | `surfaceContainer`, corners 12 dp, icons before the labels |
| Name dialogs        | Native alert with a text field                            | Material dialog with an outlined text field      |
| Deleted             | Capsule on `inverseSurface`, Undo in `inversePrimary`     | Snackbar, Undo in `inversePrimary`               |
| One-route sheet     | Liquid Glass, inset from the edges                        | Solid, `surfaceContainerLow`, full width         |
| Summary             | Translucent card, corners 22 pt                           | `surfaceContainerHigh`, corners 16 dp            |
| Buttons             | 50 pt                                                     | 48 dp                                            |

- **Built with:** shared Compose, styled per platform as above; the menu and swipe are the same Compose code on
  both. On iOS only the list's sheet, its navigation bar and the alerts are native
  ([Architecture](../../architecture.md#shared-and-native-ui)).
- Delete is in the error colour in the menu.
- Screen readers hear the more button as "More for Karlštejn Castle", and the fun meter as "Fun 5 of 5".

## Copy

| Where                      | Text                                                              |
|----------------------------|-------------------------------------------------------------------|
| List title                 | Saved routes                                                      |
| Row                        | From Smíchov · 1 h 05 · 58 km                                     |
| Menu                       | Rename / Delete                                                   |
| Empty                      | No saved routes yet / Save a route while planning to drive it again later. |
| Name already used          | Name this route / You already have a route called Karlštejn Castle. |
| Rename                     | Rename route                                                      |
| Name taken, while renaming | You already have a route with this name.                          |
| Deleted                    | Route deleted / Undo                                              |
| Offline, saving            | You're offline. Saving needs a connection.                        |
| Offline, changes           | You're offline. Changes need a connection.                        |
| Offline, nothing yet       | You're offline / Saved routes need a connection. / Try again      |
| Start                      | From Smíchov                                                      |
| Starts elsewhere           | Starts 12 km from you. Apple Maps takes you there first.          |
| No location access         | Starts in Smíchov.                                                |
| Vignette                   | Needs a motorway vignette                                         |
| Buttons                    | Saved / Drive                                                     |

## Open questions

1. **Roads change:** a saved route keeps its roads. When one closes for good, the maps app goes around it, off the
   fun roads. Whether saved routes should be checked now and then, and how the user hears that one changed.
