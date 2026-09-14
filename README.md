# Candid

Candid is a Fabric film-photography mod for Minecraft Java Edition. It turns photography into a compact survival gameplay loop: craft a rangefinder, load a 36-exposure roll, meter the scene, choose aperture and shutter speed, shoot an exposed negative, develop it, and hang the finished photograph in an item frame.

## Target
- Minecraft Java **1.21.10**
- Fabric Loader **0.19.5+**
- Fabric API **0.138.4+1.21.10**
- Java **21**
- Single-player / integrated server and Fabric multiplayer
- Steam Deck-friendly viewfinder controls

## Included in 0.1
- Candid 35 rangefinder-style camera with a custom 3D item model
- 36 exposures per roll
- Candid Daylight 100, Sun 200, Portrait 400, Night 800, and Mono 400 film
- Apertures f/1.4, 2, 2.8, 4, 5.6, 8, 11, 16
- Shutters 1/15 through 1/1000
- Live reflected light meter in the viewfinder
- Film-specific contrast, saturation, color bias, exposure response, and grain
- Actual framebuffer photograph capture, downsampled into the Minecraft map palette
- Persistent exposed color-negative maps
- Darkroom Basin + Developer Chemistry processing
- Positive finished prints that are normal filled maps and can be displayed in item frames
- Photo Enlarger + Photo Paper for duplicate prints
- In-game Candid Field Guide with visual crafting-grid diagrams
- Keyboard + native GLFW gamepad input in Candid screens

The film profiles are original Candid looks inspired by the broad visual character of classic consumer, portrait, high-speed, and monochrome film. No third-party film logos or textures are copied.

## Steam Deck / Prism Launcher
1. Create or edit a Prism instance for Minecraft 1.21.10.
2. Install Fabric Loader for that instance.
3. Add Fabric API for 1.21.10.
4. Put the built `candid-0.1.0.jar` in the instance's `mods` folder.
5. Launch Minecraft. No Paper server is required for single player.

Inside the Candid viewfinder: **D-pad Up/Down** changes aperture, **D-pad Left/Right** changes shutter, **A** fires, and **B** closes. The guide uses D-pad/bumper navigation. Steam Input can still be used for the rest of Minecraft's normal mouse/keyboard controls.

## Quick play loop
1. Craft a Candid 35 camera and a film roll.
2. Hold the camera and **Sneak + Use** to load the first compatible roll in your inventory.
3. **Use** the camera normally to enter the viewfinder.
4. Adjust aperture/shutter until the meter is near 0, then press the shutter.
5. The shot appears as an exposed negative map.
6. Carry Developer Chemistry and use the negative on a Darkroom Basin.
7. The developed positive is a normal Minecraft filled map: frame it on a wall or carry it.
8. Use a developed print on a Photo Enlarger while carrying Photo Paper to make copies.

## Building
GitHub Actions builds the mod automatically. A local developer can also run `gradle build` with Gradle 9.5.1 and Java 21; a Gradle wrapper is added to release-ready branches once the build is validated.
