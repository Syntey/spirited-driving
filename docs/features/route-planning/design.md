# Route planning screen

> **Status:** partly built. Last updated 2026-11-13.
>
> What's left to build of [Route planning](README.md): Save, shown but disabled until
> [Account](../account/README.md) exists. The rest is built; the decisions behind it are
> in the README's [Design](README.md#design) section. Uses the [Theme](../../theme.md).

## Save

- Tonal, next to Drive. Planning, not covered, offline and failed disable it, as they do Drive.
- Signed out, it asks the user to sign in ([Navigation](../../navigation.md#sign-in)).
- Once saved, it reads "Saved" with a filled bookmark, and tapping it again un-saves the route.
- When the destination's name is already used by a saved route, it asks for a name first
  ([Saved routes](../saved-routes/design.md#saving)).

## Copy

| Where                | Text                                 |
|----------------------|--------------------------------------|
| Save                 | Save / Saved                         |
