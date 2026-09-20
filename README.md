# Candid

## Candid 0.4 — Lumen35 integration

The Lumen35 exposure/meter core is now integrated into Candid. This version adds
scene-weighted metering, matching 3:2 viewfinder/capture framing, HUD-free capture,
partial-roll rewind/unload/reload, persistent negatives, 20-second whole-roll
development, contact sheets, and repeat printing for one Photo Paper. New prints
are locked maps, preserving their image when held or displayed.

Install `candid-0.4.0.jar` in a **Minecraft 1.21.10 Fabric** instance alongside
Fabric API **0.138.4+1.21.10**. Remove the older Candid JAR. The artwork is bundled;
single player needs no separate server. For a first test, create a creative world
and use the Candid Photography creative tab. The in-game Field Guide explains
every step and the actual recipes.

**Updated workflow:** load → wind → expose → rewind/unload → use roll on basin
with developer → wait 20 seconds → use developed roll → click frame with paper.
In camera-body controls, **Y / U / Rewind button** unloads. Partial rolls retain
their frames. Right stick or right-mouse drag aims inside the finder.

See [integration, upgrade and testing notes](docs/INTEGRATION.md).

Candid is a Fabric film-photography mod for Minecraft Java Edition. It turns photography into a compact survival gameplay loop: craft a rangefinder, load a 36-exposure roll, meter the scene, choose aperture and shutter speed, manually advance the film, expose a roll, develop it, and hang the finished photograph in an item frame.

## Target
- Minecraft Java **1.21.10**
- Fabric Loader **0.19.5+**
- Fabric API **0.138.4+1.21.10**
- Java **21**
- Single-player / integrated server and Fabric multiplayer
- Steam Deck-first camera controls

## Features
- Candid 35 mechanical rangefinder with a rebuilt detailed 3D model: stepped lens barrel, finder/rangefinder windows, top plate, shutter dial/button, rewind knob, advance lever, strap lugs, leatherette body, and back-door seam
- 36-exposure film rolls
- Seven 36-exposure stocks: Vivid 100, Golden 200, Everyday 400, Portrait 400, Portrait 800, Classic Mono 400, and Fine Mono 400
- Apertures f/1.4, 2, 2.8, 4, 5.6, 8, 11, 16
- Shutters 1/15 through 1/1000
- Live reflected light meter in the viewfinder
- Manual film advance: after every exposure the camera must be wound before it can fire again
- Camera-body control screen with physical-style aperture/shutter dials, film selector, and advance lever
- Animated film loading: camera back opens, cartridge enters the chamber, leader stretches to the take-up spool, back closes, and the camera advances to frame 1
- Mechanical Candid shutter, advance, film-loading, and back-latch foley
- Film-response simulation with stock-specific highlight shoulder, shadow toe, under/overexposure tolerance, contrast, saturation, color response, luminance/chroma grain, and extra shadow grain when underexposed
- Actual framebuffer photograph capture, downsampled into the Minecraft map palette
- Persistent negatives on uniquely identified film rolls, with 36 frame slots
- Darkroom Basin + Developer Chemistry whole-roll processing in 20 seconds
- Contact sheets and reusable negatives; locked map prints display in ordinary item frames
- Photo Enlarger + Photo Paper for duplicate prints
- In-game Candid Field Guide with visual crafting-grid diagrams and controller help

The film profiles use original Candid names and art, with behavior informed by published Kodak characteristics such as GOLD 200, ULTRA MAX 400, PORTRA 400/800, TRI-X 400, and T-MAX 400. They are not claimed as exact colorimetric emulations. See `FILM_REFERENCE.md` for the reference methodology. No third-party film logos, packaging, or textures are copied.

The audio included in the repository is original Candid mechanical foley. See `SOUND_ASSETS.md` for provenance and vetted CC0 real-camera reference recordings that can be substituted in a future audio pass.

## Steam Deck / Prism Launcher
1. Create or edit a Prism instance for Minecraft 1.21.10.
2. Install Fabric Loader for that instance.
3. Add Fabric API for 1.21.10.
4. Put `candid-0.4.0.jar` in the instance's `mods` folder.
5. Launch Minecraft. No Paper server is required for single player.

### Camera controls

**In the world**
- **Use** — raise the camera / open the viewfinder.
- **Crouch + Use** — open the physical camera-body controls.

**Viewfinder**
- **D-pad Up/Down** — aperture.
- **D-pad Left/Right** — shutter speed.
- **A** — fire the shutter.
- **X** — manually wind/advance the film.
- **Y** — open camera-body controls.
- **B** — lower the camera.

**Camera-body controls**
- **D-pad Left/Right** — select aperture dial, shutter dial, film, or advance lever.
- **D-pad Up/Down** — turn the selected dial / change the selected film stock.
- **A** — operate the selected control. On FILM this begins the visible loading sequence.
- **X** — wind the advance lever from anywhere on the body screen.
- **Y / U / Rewind button** — rewind and unload the current roll.
- **B** — close.

The film-loading animation may be skipped with **B**; if you skip before the initial wind finishes, press **X** once to ready frame 1.

Keyboard equivalents are arrows for dials, Enter/Space for shutter/operate, **R** for wind, and **C** for body controls.

## Quick play loop
1. Craft a Candid 35 and a film roll.
2. Crouch + Use the camera, select **FILM**, choose a roll, and press **A**.
3. Watch the camera back open, load the cartridge and leader, close, and advance.
4. Use the camera normally to enter the viewfinder.
5. Meter the scene and press **A** to expose a frame.
6. Press **X** to wind before the next photograph.
7. Rewind/unload from the controls with **Y / U**; partial rolls retain their frames.
8. Use the roll on the Darkroom Basin with one Developer Chemistry, then wait 20 seconds.
9. Use the roll to open the contact sheet; click a frame with Photo Paper in your inventory.
10. Each print costs one sheet. Repeat from the same negative, trade it, or display it in an item frame.

## Building
GitHub Actions builds the mod automatically. The repository includes the Gradle wrapper, so a local developer with Java 21 can run `./gradlew build` (or `gradlew.bat build` on Windows).
