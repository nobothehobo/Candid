# Candid

Candid is a Fabric film-photography mod for Minecraft Java Edition. It turns photography into a compact survival gameplay loop: craft a rangefinder, load a 36-exposure roll, meter the scene, choose aperture and shutter speed, manually advance the film, shoot an exposed negative, develop it, and hang the finished photograph in an item frame.

## Target
- Minecraft Java **1.21.10**
- Fabric Loader **0.19.5+**
- Fabric API **0.138.4+1.21.10**
- Java **21**
- Single-player / integrated server and Fabric multiplayer
- Steam Deck-first camera controls

## Included in 0.2
- Candid 35 rangefinder-style camera with a custom 3D item model
- 36-exposure film rolls
- Candid Daylight 100, Sun 200, Portrait 400, Night 800, and Mono 400
- Apertures f/1.4, 2, 2.8, 4, 5.6, 8, 11, 16
- Shutters 1/15 through 1/1000
- Live reflected light meter in the viewfinder
- Manual film advance: after every exposure the camera must be wound before it can fire again
- Camera-body control screen with physical-style aperture/shutter dials, film selector, and advance lever
- Animated film loading: camera back opens, cartridge enters the chamber, leader stretches to the take-up spool, back closes, and the camera advances to frame 1
- Mechanical Candid shutter, advance, film-loading, and back-latch foley
- Film-specific contrast, saturation, color bias, exposure response, and grain
- Actual framebuffer photograph capture, downsampled into the Minecraft map palette
- Persistent exposed color-negative maps
- Darkroom Basin + Developer Chemistry processing
- Positive finished prints that are normal filled maps and can be displayed in item frames
- Photo Enlarger + Photo Paper for duplicate prints
- In-game Candid Field Guide with visual crafting-grid diagrams and controller help

The film profiles are original Candid looks inspired by the broad visual character of classic consumer, portrait, high-speed, and monochrome film. No third-party film logos or textures are copied.

The audio included in the repository is original Candid mechanical foley. See `SOUND_ASSETS.md` for provenance and vetted CC0 real-camera reference recordings that can be substituted in a future audio pass.

## Steam Deck / Prism Launcher
1. Create or edit a Prism instance for Minecraft 1.21.10.
2. Install Fabric Loader for that instance.
3. Add Fabric API for 1.21.10.
4. Put `candid-0.2.0.jar` in the instance's `mods` folder.
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
7. Develop exposed negatives in the Darkroom Basin with Developer Chemistry.
8. Frame the positive map print or duplicate it with the Photo Enlarger + Photo Paper.

## Building
GitHub Actions builds the mod automatically. The repository includes the Gradle wrapper, so a local developer with Java 21 can run `./gradlew build` (or `gradlew.bat build` on Windows).
