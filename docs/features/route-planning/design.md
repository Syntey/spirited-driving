# Route planning screen

> **Status:** partly built. Last updated 2026-11-13.
>
> What's left to build of [Route planning](README.md): Save and Drive, shown but disabled until
> [Account](../account/README.md) and [Drive](../drive/README.md) exist. The rest is built; the decisions behind it are
> in the README's [Design](README.md#design) section. Uses the [Theme](../../theme.md).

## Save and Drive

- **Save** (tonal) and **Drive** (filled, wider) sit at the bottom of the sheet, with "Route data © OpenStreetMap
  contributors" below them. On Android, "Routing provided by Spirited Driving" comes first
  ([Drive](../drive/design.md#routing-provided-by-android)).
- Planning, not covered, offline and failed disable both.

## Save

- Signed out, it asks the user to sign in ([Navigation](../../navigation.md#sign-in)).
- Once saved, it reads "Saved" with a filled bookmark, and tapping it again un-saves the route.
- When the destination's name is already used by a saved route, it asks for a name first
  ([Saved routes](../saved-routes/design.md#saving)).

## Copy

| Where                | Text                                 |
|----------------------|--------------------------------------|
| Save                 | Save / Saved                         |
| Attribution, Android | Routing provided by Spirited Driving |
