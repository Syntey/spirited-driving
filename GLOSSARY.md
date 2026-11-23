# Spirited Driving

A navigation app that finds the most enjoyable route from A to B within the extra time or distance the user is
willing to spend.

## Routes

**Baseline route**:
The fastest route from A to B, which the budget is measured against.
_Avoid_: Default route. "Fastest" is fine as its label in the app and its name in the code.

**Budget**:
How much longer than the baseline route a route may be, in minutes or in kilometres.
_Avoid_: Detour, allowance, extra

**Fun route**:
A route within the budget, chosen for how enjoyable its roads are to drive.
_Avoid_: Scenic route, curvy route

## Planning

**Trip**:
One outing from a start to a destination that the user is planning: its start, destination, budget and the routes
found for it.
_Avoid_: Journey, the map screen

**Route planning**:
The step of a trip from a budget to the list of routes to choose from.
_Avoid_: Trip plan (for the whole map screen)

**Sheet step**:
What the sheet over the map shows at one time: the destination search, the start search, route
planning, one route, or the rating of a drive.
_Avoid_: Page, mode

**One route**:
A single route shown on the map without planning it again: a saved route, or a past drive from Trip history.
_Avoid_: Route detail
