# Drive feedback screen

> **Status:** draft, being iterated. Last updated 2026-11-08.
>
> The design for [Drive feedback](README.md): the last step of the map's sheet after a drive, a reminder bubble on
> the map, and a quiet notification. Uses the [Theme](../../theme.md); the map and its sheet are described in the
> [Map screen](../map/design.md).

## Where it shows

| The user comes back…                       | They see                                                                      |
|--------------------------------------------|-------------------------------------------------------------------------------|
| Before half the route's duration           | The route, still in the sheet and ready to drive ([Drive](../drive/README.md#design)) |
| Soon after the drive                       | The rating step in the sheet                                                  |
| Later                                      | The search step, with the reminder bubble on the map                          |
| From the notification                      | The rating step                                                               |

Where "soon" ends is open question 1. If Profile or Saved routes is open over the map, the rating step waits until
it closes.

## Rating step

**Map**

- Only the driven route, in its [route colour](../../theme.md#route-colours) and drawn as selected, without a label.
- A ring marks the start, a pin the destination; the user's location dot shows where they are now.
- The camera fits the route into the part of the map above the sheet.
- The profile and locate buttons are hidden, as while planning.

**Sheet**, top to bottom:

1. **Header:** "How was the route to Karlštejn Castle?" and a close button. Below it, the route's badge (its number
   in its colour) and the drive: "Today, 14:20 · 1 h 05 · 58 km", the hand-off time with the planned time and
   distance. No fun meter, so the planned score doesn't sway the answer.
2. **Thumbs:** "Good" and "Not good", two equal buttons side by side.
3. **Before a thumb is picked:** "I didn't drive it", a text button.
4. **After a thumb is picked:** a thanks line, the reason chips for that thumb, and **Done**.
5. **Attribution:** "Route data © OpenStreetMap contributors". On Android, "Routing provided by Spirited Driving"
   comes first, as on every Android sheet that shows a route
   ([Drive](../drive/README.md#design)).

**Behaviour**

- Tapping a thumb saves the rating at once; the thumb fills and the chips appear. Tapping the other thumb switches
  the rating and its chips, and clears the reasons picked.
- Reasons are optional, and any number can be picked. Whatever is picked is kept however the step closes.
- Done, the close button and Back (Android) go to the search step. The trip is over, so the route leaves the map.
- "I didn't drive it" removes the drive from [Trip history](../trip-history/README.md) and goes to the search step,
  with no confirmation.
- Tapping a thumb while the sheet is collapsed raises it to half height.
- The same content, without the map, rates a past drive in
  [Trip history](../trip-history/design.md#rating-sheet).

## Reasons

| After Good            | After Not good             |
|-----------------------|----------------------------|
| Curvy roads           | Boring roads               |
| Nice views            | Too many towns             |
| Little traffic        | Too narrow                 |
| Smooth surface        | Rough surface              |
| Worth the extra time  | Heavy traffic              |
|                       | Roadworks                  |
|                       | Took too long              |
|                       | Maps app went another way  |

- Chips wrap onto as many lines as they need, in this order: the roads first, then the trip.
- **Too narrow** is about cars: single-track lanes are fun on a motorbike and tiring in a car.
- **Maps app went another way** means the maps app left the planned roads, so the rating isn't held against the
  route ([Architecture, test cases](../../architecture.md#test-cases)).
- **Heavy traffic** and **Roadworks** aren't the route's fault. Having them keeps those drives out of "Took too
  long".
- No free text.

## Reminder bubble

Shows when the user comes back later than "soon" and hasn't rated the drive yet.

- A capsule over the map, just above the collapsed sheet: the route's badge, "How was the route?" with
  "Karlštejn Castle · Yesterday" below it, and a dismiss button.
- Tapping it opens the rating step. Dismissing hides it for good; the drive can still be rated in
  [Trip history](../trip-history/README.md).
- It shows only on the search step with the sheet collapsed. It hides while the sheet is higher or on another step,
  and comes back when the sheet collapses on the search step again.
- While it shows, the map padding grows by its height, so the map's logo and legal notice stay visible.
- A new drive replaces it.

|        | iOS                                            | Android                                                    |
|--------|------------------------------------------------|------------------------------------------------------------|
| Look   | Native glass, like the map buttons             | `surfaceContainerHigh` with the map buttons' shadow        |
| Place  | Centred, 12 pt above the sheet                 | Left, in line with the locate button and clear of it       |
| Height | 56 pt                                          | 56 dp                                                      |

The destination name truncates; the first line never does.

## Notification

Scheduled at the hand-off, for the planned arrival.

- **Title:** "How was the route to Karlštejn Castle?" **Body:** "Rate it to help find better roads."
- Tapping it opens the app on the rating step.
- It never lights up the screen, pops up or makes a sound, so it can't distract while driving.
    - **iOS:** provisional authorization, so there's no permission prompt; iOS delivers it quietly to Notification
      Centre and offers Keep or Turn Off there. Interruption level passive.
    - **Android:** a "Drive reminders" channel with low importance, listed under Silent. Android 13 and later need
      the notification permission ([below](#reminder-offer-android)).
- It's removed when the drive is rated, skipped or dismissed anywhere, or replaced by a new drive.
- It isn't scheduled for drives that won't be asked about ([README](README.md#rules)).

## Reminder offer (Android)

On Android 13 and later, the first time the user picks a thumb, a row above Done offers the notification: a bell,
"Get a reminder after each drive", "A quiet notification, no sound", and **Turn on**, which shows the system
permission prompt. It shows once, whatever the answer, and not at all when notifications are already allowed.

## Sheet heights

| Height        | Shows                                  |
|---------------|----------------------------------------|
| Half, default | Everything above                       |
| Collapsed     | The header and the thumbs              |

## Platforms

|                     | iOS                                         | Android                                          |
|---------------------|---------------------------------------------|--------------------------------------------------|
| Sheet               | Liquid Glass, inset from the edges          | Solid, `surfaceContainerLow`, full width         |
| Thumbs              | Tinted fill, 56 pt                          | Outlined, 56 dp                                  |
| Picked thumb        | `tertiaryContainer`, filled icon            | `tertiaryContainer`, filled icon                 |
| Reason chips        | Tinted fill                                 | Outlined filter chips                            |
| Picked chip         | `tertiaryContainer` with a check            | `tertiaryContainer` with a check                 |
| I didn't drive it   | Plain button in primary                     | Text button                                      |
| Done                | Filled, 50 pt                               | Filled, 48 dp                                    |

Picked thumbs and chips are amber because the theme gives ratings to tertiary; red stays for Done. Screen readers
hear the thumbs as a group, "Rate the route", with "Good, selected".

## Copy

| Where                  | Text                                                         |
|------------------------|--------------------------------------------------------------|
| Question               | How was the route to Karlštejn Castle?                       |
| Drive                  | Today, 14:20 · 1 h 05 · 58 km (or Yesterday, or the date)   |
| Thumbs                 | Good / Not good                                              |
| Not driven             | I didn't drive it                                            |
| After Good             | Thanks! What stood out?                                      |
| After Not good         | Thanks. What didn't work?                                    |
| Button                 | Done                                                         |
| Bubble                 | How was the route? / Karlštejn Castle · Yesterday            |
| Notification           | How was the route to Karlštejn Castle? / Rate it to help find better roads. |
| Reminder offer         | Get a reminder after each drive / A quiet notification, no sound / Turn on |

## Open questions

1. **Soon or later:** how long after the planned arrival the rating step still opens on its own before the bubble
   takes over, and how long the bubble lasts.
2. **Notification time:** the planned arrival leaves out stops and the way to a start elsewhere. How much margin
   to add.
