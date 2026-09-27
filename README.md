# Candid 0.6 — film photography for Minecraft

Craft a mechanical camera, load a 36-shot roll, meter and photograph your world,
then develop, preview, print and share your pictures. This is the Candid **Fabric
mod for single player and Fabric multiplayer**, not a Paper plugin.

## Install in Prism / Steam Deck

1. Choose a matching Minecraft / Java / Fabric API combination from the table below.
   Use **Fabric Loader 0.19.5+**. In Prism, edit the instance to select Fabric and
   the appropriate Java runtime (21 for 1.21.x; 25 for 26.x).
2. Back up your world. Remove the older Candid JAR from this instance's `mods` folder.
3. Put the **Candid JAR labeled with your exact Minecraft version** in `mods` and launch. All models, textures and audio
   are included. Single player needs no separate server.
4. Open the **Candid Photography** creative tab for a quick test, or craft normally.
   The **Candid Field Guide** has instructions and diagrams for all 19 shipped recipes.

For multiplayer, both client and server need the same Candid version. Optional
Iris/Sodium and shader packs are client-side; they are not bundled or required.

## Minecraft versions

| Minecraft | Java | Fabric API baseline | Candid file |
| --- | --- | --- | --- |
| 1.21.10 | 21 | 0.138.4+1.21.10 | `candid-0.6.0+mc1.21.10.jar` |
| 1.21.11 | 21 | 0.141.6+1.21.11 | `candid-0.6.0+mc1.21.11.jar` |
| 26.1.2 | 25 | 0.155.3+26.1.2 | `candid-0.6.0+mc26.1.2.jar` |
| 26.2 | 25 | 0.161.0+26.2 | `candid-0.6.0+mc26.2.jar` |
| 26.3 | 25 | 0.161.0+26.3 | `candid-0.6.0+mc26.3.jar` |

Install **one** Candid JAR per instance. These are separate builds of the same mod,
not five mods to install together. Future Minecraft releases and snapshots need
verification and their own release; changing the filename does not make a JAR compatible.
For the 26.1 series, use its final patch **26.1.2**.

## What changed in 0.6

- Separate builds share the same film, optics, darkroom, image processing and saves.
- Updated screens, live finder and animated hands for modern rendering.
- Tripods now hold the real camera, save pan/tilt, and support a paired Cable Release.
- Separate mounted lens barrels, animated rewinding and a brief raise-to-eye gesture.
- A separately pivoting advance lever follows a skinned-hand winding stroke without
  leaving the finder or making a ready shutter wait for the animation.
- Selected focus distance plus aperture/lens-dependent **Near / Far** depth-of-field
  limits, including infinity, with matching green subject confirmation.
- Finished rolls open away from the basin, including when used on ordinary terrain.
- Shutter feedback starts immediately; depth sampling no longer delays the capture.
- SDL gamepad support on 26.3; GLFW support retained for earlier releases.
- Version labels and exact Minecraft requirements prevent accidental cross-version installs.
- The complete single-player workflow is exercised independently by the CI version matrix.

See [compatibility and upgrade notes](docs/COMPATIBILITY.md) and
[the test report](docs/TEST_REPORT.md) for validation status.

## Photography features (carried forward from 0.5)

- **Clickable darkroom stations:** film and chemistry/paper input slots, development
  progress, safe supply return when closing, free frame previews, repeat printing.
- **Full-color 504 × 336 scans** with PNG export. New held prints reopen their scans.
  Ordinary maps remain palette-limited; an enlarger can make a matted 2 × 2 map display.
- **Lighting-aware metering:** scene reflectance, sunlight direction and shade,
  time, rain and local light. Exposure errors visibly affect tone and shadow grain.
- **Optics:** interchangeable 28/35/50/90 mm lenses, matching finder/capture framing,
  focus distance, focus confirmation and depth-of-field approximation in the final scan.
- **Tripod exposures:** aim from a fixed tripod head and combine samples over
  1/2/4/8/15/30 seconds. Long exposures require a tripod.
- **Physical loading:** camera back, cartridge, leader and the player's skinned
  hands animate in the world. Skipping the animation does not undo loading.
- **Real recorded film shutter**, with documented CC0 provenance; original wind/latch foley.
- **Input fixes:** shutter input stays captured so it does not trigger jumping;
  load/unload requests no longer depend on reaching the end of an animation.

## Quick play loop

1. Craft a camera, a film roll, tank, developer, enlarger and Photo Paper.
   Materials are common overworld supplies; every film craft makes a complete
   36-exposure roll, and paper crafts in batches.
2. **Crouch + Use** the camera. Choose the film stock, then **Load selected film**.
   The corresponding roll must be in your inventory. Loading normally winds frame 1.
3. **Use** the camera to enter the finder. Aim, adjust aperture/shutter and focus.
   Aim near **0 EV** for a balanced exposure. ISO belongs to the loaded film.
4. Fire, then **wind** before the next frame. Rewind/unload full or partial rolls
   from the controls; a partial roll keeps its original frames when reloaded.
5. Click the **Darkroom Basin / Developing Tank**. Put exposed film in the first
   input slot and one **Developer Chemistry** in the second. Click **Start**.
   Processing takes 20 seconds. Closing returns your roll and supplies safely;
   development continues on the roll.
6. Click the **Photo Enlarger**. Insert the developed roll and Photo Paper.
   Select a frame for a **free preview**. Print only when happy: one sheet per
   print, including a complete four-map large print. Negatives are reusable.
7. Use a developed roll for its contact sheet: **right-click a frame to preview**,
   **left-click to print** using paper from your inventory.
8. Display ordinary prints in item frames. For a large print, arrange its four
   labeled maps in a 2 × 2 square, all in the same rotation.

## Sharing outside Minecraft

Open a negative preview, or **Use a newly printed photo**, then choose **Export PNG**.
The mod saves the positive scan in **`.minecraft/candid-exports`** inside that
Prism instance and opens the folder. Share the PNG through Discord, messages,
a photo library or any app you normally use. There is no automatic posting,
account setup, upload service or embedded player location in the exported image.
Old negatives can export their existing map proof; updating cannot recreate
full-color detail that was never stored.

**Scan / map proof** compares the full-color image with the actual map rendering.
**Positive / negative** is a viewing aid; export always saves the positive scan.

## Controls

| Action in viewfinder | Keyboard / mouse | Gamepad |
| --- | --- | --- |
| Aim | Hold right mouse and drag | Right stick |
| Aperture | Up / Down | D-pad Up / Down |
| Shutter speed | Left / Right | D-pad Left / Right |
| Fire | Enter / Space | A |
| Wind film | R | X |
| Camera controls | C | Y |
| Focus nearer / farther | Mouse wheel or [ / ] | Left / right bumper |
| Match center subject distance | F | Left-stick click |
| Close / cancel exposure | Escape | B |

Camera controls use real buttons: click, **Tab + Enter**, or **D-pad + A**.
**U / Y** operates load/unload there. If you skip loading before winding,
use **Wind / advance** once. The center focus patch turns green when the
subject is inside the displayed Near / Far range. Stop down (higher f-number) to
widen it. These are approximate 35 mm depth-of-field limits using a 0.03 mm circle
of confusion, not a hard sharpness boundary or autofocus tracking.

Place a tripod and **Use it while holding the camera**. The camera leaves your hand
and stays on the stand. Use the occupied stand to compose; right-drag or the right
stick adjusts its saved pan/tilt. Choose a seconds-long shutter for night exposures.
Crouch + Use the stand to retrieve the camera with its existing roll and settings.

Craft a **Cable Release** from a stone button, copper ingot and string in a vertical
column. Use it on the mounted camera to pair, then use the release within 32 blocks
to enter its finder and fire. It works only in the same dimension and loaded area.
Walk up to the stand to change film/lenses or retrieve the camera. Breaking a stand
returns its camera; camera custody and angles persist with the world.
The camera remains in your hand; this version has no unattended timed shutter.

Steam Input can expose either a gamepad or keyboard/mouse controls. Avoid
assigning the same physical button through both paths. Physical Deck/controller
validation remains part of the manual checklist.

## Film and image character

Seven original stocks: **Vivid 100, Golden 200, Everyday 400, Portrait 400,
Portrait 800, Classic Mono 400, Fine Mono 400**. Golden is warmer and restrained;
Portrait has softer contrast; monochrome stocks differ in grain and tone.
See [film references](FILM_REFERENCE.md). These are inspired profiles, **not
measured or licensed Kodak emulations**. No film trademarks or packaging are copied.

Capture uses the actual rendered Minecraft view, so active shaders and resource
packs can appear in photographs. The meter estimates world illumination separately;
a shader's own exposure, tone curve, depth of field or motion blur can affect the result.
There is no access to physical HDR scene radiance or recovery of already-clipped highlights.
See [test report](docs/TEST_REPORT.md) for the tested Iris setup and remaining limits.

## Build, assets and technical notes

With Java 21 (1.21.x targets) or Java 25 (all targets), and Python 3:

```sh
bash gradlew build -Ptarget=26.3
xvfb-run -a bash gradlew runClientGameTest -Ptarget=26.3
```

The installable artifact is `build/26.3/libs/candid-0.6.0+mc26.3.jar`.
Replace `26.3` with your chosen supported target. The default is `1.21.10`.
GitHub Actions builds and tests each version; download the matching
`Candid-0.6.0-mc<version>` artifact after its workflow passes.

Editable models are in `assets/blockbench`; run `python3 tools/export_models.py`
to export them. See [assets](assets/README.md), [audio provenance](SOUND_ASSETS.md),
[integration and performance](docs/INTEGRATION.md), [tests](docs/TEST_REPORT.md),
and [future StateCraft adapter work](docs/STATECRAFT_IMPORT.md).
