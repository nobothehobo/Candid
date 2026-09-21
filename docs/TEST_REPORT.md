# Candid 0.4 integration validation

## Automated checks

On September 20, 2026, GitHub Actions successfully built the release JAR,
ran all **20 unit tests**, and ran the actual Minecraft 1.21.10 client with
an integrated single-player server. Initial passing evidence:
[workflow 35522075060](https://github.com/nobothehobo/Candid/actions/runs/35522075060).
Every subsequent PR push reruns these checks; use the final green PR run for
the downloadable release JAR and screenshots.
The final suite adds a 21st regression test for normalized model UV coordinates.

The client test exercises:

- Film loading through the real client/server payload, including initial winding.
- Framebuffer capture and a nonblank 128 × 128 map-palette result.
- Frame consumption and the requirement to wind again after exposure.
- Unloading and reloading the same partially exposed roll without resetting it.
- Consumption of developer and the real 20-second development delay.
- Opening the contact sheet and printing twice for exactly two paper sheets.
- A locked map whose palette bytes exactly match the captured frame.
- Saving, leaving, reopening the world, and verifying the same roll, map ID and pixels.

Unit tests cover photographic stops, ISO, scene luminance, exposure response,
film capacity and custody, development transitions, repeated printing,
serialization, malformed storage, ordered writes, and frame geometry.
JSON/model checks also validate the custom texture references and editable
Blockbench source files. Test classes are excluded from the release mod.

## Fixes found during validation

- Fabric's synchronized test-network mode stalled during 1.21.10 login. Its
  supported asynchronous mode passes; tests wait for state changes explicitly.
- Screenshot review revealed unwanted vanilla menu blur in the viewfinder.
  The finder now renders a sharp overlay without the menu background pass.
- Film models lacked a particle texture reference; the common parent now supplies it.
- The contact-sheet title and film-selector text exceeded their intended width.
- Timed-out captures could interfere with later captures; generation tokens
  now discard stale completion callbacks.
- A real held-camera screenshot exposed pixel-space UVs in the imported Java
  models. The reproducible exporter now normalizes them to Minecraft's 0–16
  range; editable Blockbench sources retain their correct pixel-space UVs.

## Not yet manually validated

- A physical Steam Deck, its Steam Input configuration, and third-party controller mods.
- Shader packs, alternative renderers and every GUI scale/display aspect ratio.
- Audible quality on real speakers/headphones; CI has no audio device.
- Large multiplayer archives, long-session profiling and hard-crash recovery.

The CI runner reports unavailable narrator/audio devices and an X11 cursor
warning. These are not successful audio/controller tests. Startup can also
produce a server catch-up warning on the software-rendered runner; this is not
a frame-time benchmark. See INTEGRATION.md for bounded-work design and storage limits.

## Manual acceptance checklist

- [ ] In survival, craft the camera, all film stocks, developer, station, paper and guide.
- [ ] Compare clear noon, sunset, night, rain, indoor shade and torchlight readings.
- [ ] Photograph a recognizable subject at −2, −1, 0, +1 and +2 EV.
- [ ] Check finder edges against the print at multiple GUI scales and FOV settings.
- [ ] Fill all 36 frames; verify a 37th shot is rejected.
- [ ] Unload a partial roll, transfer it, store it in a chest and reload it later.
- [ ] Restart with partially exposed and developing rolls, then continue normally.
- [ ] Print repeatedly, trade prints and display them in item frames.
- [ ] Run the same loop using the Steam Deck controls and the in-game guide alone.
- [ ] Back up an older Candid world, update it, and verify legacy loose negatives still work.
