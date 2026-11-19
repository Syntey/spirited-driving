# Saved routes screens

> **Status:** partly built. Last updated 2026-11-17.
>
> What's left to build of [Saved routes](README.md): two things the app needs to be told about each route. The rest is
> built; the decisions behind it are in the README's [Design](README.md#design) section. Uses the
> [Theme](../../theme.md).

## List order

- Last used first, saved or driven, whichever is later, so routes driven often stay on top. Until each route says when
  it was last driven, the list is newest saved first.

## Vignette note

- On a saved route on the map, under the summary: "Needs a motorway vignette", when the route uses motorways that need
  one, whatever Settings say. Waits for each route to list the vignette countries it uses.

## Copy

| Where    | Text                      |
|----------|---------------------------|
| Vignette | Needs a motorway vignette |

## Open questions

1. **Roads change:** a saved route keeps its roads. When one closes for good, the maps app goes around it, off the
   fun roads. Whether saved routes should be checked now and then, and how the user hears that one changed.
