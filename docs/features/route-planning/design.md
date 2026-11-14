# Route planning screen

> **Status:** draft, being iterated. Last updated 2026-11-09.
>
> The screen for [Route planning](README.md): a step of the map's sheet, with the routes drawn on the map. Uses the
> [Theme](../../theme.md); the map and its sheet are described in the [Map screen](../map/design.md).

## Layout

**Map**

- Every route as a line in its [route colour](../../theme.md#route-colours), with a label on the line.
- The start (the user's location dot, or a pin when the start was picked by hand) and the destination pin.
- The camera fits all routes into the part of the map above the sheet, and fits again when the sheet moves.
- The profile and locate buttons are hidden while planning.

**Sheet**, top to bottom:

1. **Header:** the destination's name, "From My location" (or the start's name) below it, and a Cancel text button.
   A point picked on the map is called "Point on the map" until its address arrives
   ([Destination search](../destination-search/design.md#points-picked-on-the-map)).
   "From" opens the search for the start
   ([Destination search](../destination-search/design.md#two-uses-of-one-step)). Cancel goes back to the search
   step as it was. It's Cancel, not an X: an X on a sheet reads as dismissing it, but the map's sheet stays and only
   steps back.
2. **Budget:** "Extra time" (or "Extra distance") with a min / km switch, and a row of chips that scrolls sideways.
3. **Route list.**
4. **Save** (tonal) and **Drive** (filled, wider), with "Route data © OpenStreetMap contributors" below them. On
   Android, "Routing provided by Spirited Driving" comes first
   ([Drive](../drive/design.md#routing-provided-by-android)).

## Route list

- Fun routes come first, most fun first (route 1); the fastest route comes last as the reference.
- Each row has:
    - a badge in the route's colour, with its number (an arrow for the fastest)
    - the extra over the fastest route in the budget's unit ("+27 min" or "+26 km"), or "Fastest"
    - total time and distance
    - one line of facts about the roads
    - the fun meter
- **Fun meter:** five rising bars in tertiary (amber), with "Fun" below them. One scale for every route, the
  fastest included. Screen readers hear "Fun 4 of 5".
- **Selection:** route 1 is selected when the plan arrives. Tapping a row or a line on the map selects it. The
  selected row gets a ring in the route's colour. Its line draws on top and wider, and the other lines are dimmed.
- Numbers use tabular figures.

**Labels on the map:** "1 · +27 min" on each fun route and "Fastest · 38 min" on the fastest, in the route's
colour, in the budget's unit.

## Budget

- **Chips:** +15 min, +30 min, +45 min, +1 h, +1.5 h, +2 h; or +10, +20, +30, +50, +75, +100 km. These are
  placeholders until the values are settled (feature open question 1).
- The selected chip is filled with primary. Picking a chip or switching the unit plans again at once.
- Switching the unit selects the last value used in that unit, or +30 km the first time.

## Sheet heights

| Height          | Shows                                                                          |
|-----------------|--------------------------------------------------------------------------------|
| Half, default   | Everything above                                                               |
| Collapsed       | Destination, the selected route's summary and fun meter, Save and Drive, attribution |

## States

- **Planning:** header and budget work, the route list shows placeholder rows with "Looking for the most fun
  roads…", Save and Drive are disabled. The map shows the start and destination.
- **No fun route fits:** a message above the fastest route, which is selected: "No fun route fits within
  +15 min. The closest fun route needs +22 min." A tonal **Use +30 min** button below it picks the smallest chip
  that fits, in the same unit. When no chip is big enough, the button is left out and the line reads "The closest
  fun route needs more than +2 h." Drive still works.
- **Not covered, offline, planning failed:** the [empty and error layout](../../theme.md#empty-and-error-states)
  takes the list's place. The header and budget stay, and a new budget plans again. The map shows the start and
  destination; Save and Drive are disabled.
    - **Not covered:** the start or destination is outside Czechia. "Outside the covered area" and "Routes start
      and end in Czechia for now." No button.
    - **Offline:** "You're offline" and "Planning needs a connection.", with Try again.
    - **Planning failed** (server error): "Planning isn't working right now" and "The problem is on our side, not
      your connection.", with Try again.
- **Save:** signed out, it asks the user to sign in ([Navigation](../../navigation.md#sign-in)). Once saved, it
  reads "Saved" with a filled bookmark, and tapping it again un-saves the route. When the destination's name is
  already used by a saved route, it asks for a name first ([Saved routes](../saved-routes/design.md#saving)).

## Platforms

|                     | iOS                                   | Android                                  |
|---------------------|---------------------------------------|------------------------------------------|
| Sheet               | Liquid Glass, inset from the edges    | Solid, `surfaceContainerLow`, full width |
| Unit switch         | Segmented control                     | Outlined segmented button                |
| Unselected chips    | Tinted fill                           | Outlined                                 |
| Route list          | Rows on a translucent card            | Rows on the sheet                        |
| Selected row        | Ring and a lighter fill               | Ring and `surfaceContainerHighest`       |
| Buttons             | 50 pt                                 | 48 dp                                    |

## Copy

| Where                  | Text                                          |
|------------------------|-----------------------------------------------|
| Start                  | From My location                              |
| Cancel                 | Cancel                                        |
| Budget label           | Extra time / Extra distance                   |
| Fastest route          | Fastest                                       |
| Fun meter              | Fun                                           |
| Planning               | Looking for the most fun roads…               |
| No fun route           | No fun route fits within +15 min. The closest fun route needs +22 min. |
| No fun route, button   | Use +30 min                                   |
| Attribution            | Route data © OpenStreetMap contributors       |
| Attribution, Android   | Routing provided by Spirited Driving          |
| Not covered            | Outside the covered area / Routes start and end in Czechia for now. |
| Offline                | You're offline / Planning needs a connection. / Try again |
| Planning failed        | Planning isn't working right now / The problem is on our side, not your connection. / Try again |

## Open questions

1. **Fun scale:** five steps read at a glance. Check with real plans whether routes often land on the same step.
2. **Labels on the map** can overlap on short trips. Check their placement on real routes.
