# App icon

Generates the app icon for both platforms from one drawing: the [spinner's](../../docs/theme.md#progress) car on a
bend of road, on racing red ([Theme](../../docs/theme.md#app-icon)).

```sh
node tools/app-icon/generate.mjs
```

Needs Node 18 or newer and nothing else.

## Output

| File                                                    | What it is                                                                     |
|---------------------------------------------------------|--------------------------------------------------------------------------------|
| `iosApp/SpiritedDriving/Resources/AppIcon.icon/`        | Icon Composer document: the background and three layers, with the dark colours |
| `androidApp/.../drawable/ic_launcher_foreground.xml`    | Adaptive icon foreground: road, centre line and car                            |
| `androidApp/.../drawable/ic_launcher_monochrome.xml`    | Themed icon: the road with the car cut out of it                               |
| `androidApp/.../values/ic_launcher_background.xml`      | The background colour                                                          |
| `androidApp/.../mipmap-anydpi/ic_launcher.xml`          | The adaptive icon, joining the three                                           |

All of them are checked in. Change the drawing in `generate.mjs` and run it again, never edit the output by hand or
in Icon Composer: the next run overwrites it.

## How it fits together

- The drawing is a 1024 square, the iOS icon. The Android layer is 108 dp, 1536 units, with the same square in its
  middle: that's the part a launcher's mask shows, so both platforms show the same picture.
- The road is one cubic curve that leaves the square at both ends. The centre line's dashes are measured along it and
  drawn as short lines, since Android's vector drawables can't dash a stroke.
- The car has the spinner's proportions (`CarShape` in `CarSpinner.kt`), sits in the middle of the road and is
  turned along it.
- In Icon Composer each layer is its own group, so Liquid Glass gives the car depth over the road. Translucency is
  off, so the centre line doesn't show through the car. iOS makes the tinted and clear appearances itself.

## Checking it

Icon Composer, which comes with Xcode, opens `AppIcon.icon` and shows every appearance. To render one from the
command line:

```sh
ictool="/Applications/Xcode.app/Contents/Applications/Icon Composer.app/Contents/Executables/ictool"
"$ictool" iosApp/SpiritedDriving/Resources/AppIcon.icon --export-image --output-file icon.png \
  --platform iOS --rendition Dark --width 1024 --height 1024 --scale 1
```

The renditions are `Default`, `Dark`, `TintedLight`, `TintedDark`, `ClearLight` and `ClearDark`.
