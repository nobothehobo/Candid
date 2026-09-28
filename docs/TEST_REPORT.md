# Candid validation

## 0.6.1 playtest fix validation

Final release code `d4698dcb839a601f2b23b37a5373229901267675` passed all five
version builds, 52 JUnit tests per build, integrated gameplay, decoded audio and
the shader fixture in [run 36382655063](https://github.com/nobothehobo/Candid/actions/runs/36382655063).
This final run also verifies cancellation leaves the roll at 33 frames and wound,
after three completed exposures. The timer screenshot was inspected and sits
outside the photo crop. All five downloaded JARs pass ZIP integrity, exact target
metadata, repaired shutter-byte comparison and test-class exclusion checks.

The expanded 1-, 8- and 30-second exposure workflow passed on Minecraft 1.21.10,
1.21.11, 26.1.2, 26.2 and 26.3, plus the Iris/Sodium fixture, in
[run 36382372583](https://github.com/nobothehobo/Candid/actions/runs/36382372583).
This exercises actual rendered samples and checks elapsed wall-clock duration,
one frame per release and persistence after reopening the world. The reported
long-exposure failure was not reproduced in this revised capture path.

The release includes 52 JUnit tests and real client/integrated-server checks for:

- Tank custody while closed; retrieval locked during processing; actual completion
  delay; retained inventory after world reopen; one film drop when broken.
- Both contact-sheet click buttons preview without paper consumption or map allocation.
  Only explicit enlarger printing consumes paper.
- First-person capture and restoration of the previous third-person perspective.
- Above-block tripod-camera targeting, actual cable item Use/UseOn, saved head angles.
- Continuous loading-door/leader calculations and unobstructed finder winding.
- Audio decoding for all five OGG assets; shutter onset under 50 ms, peak -1.3 dBFS
  and RMS -21.0 dBFS. The old shutter decoded to near-silence and loading foley was corrupt.

Screenshots from 1.21.10 and 26.3 were inspected: separated hands, hinged back,
raise pose, focus scale, clean finder winding and a photo without selection outlines.
143 resource JSON files parse successfully; 46 editable Blockbench sources are retained.

Manual limits: no physical Deck controller/speaker test or subjective animation
smoothness measurement. Film remains an original approximation, not measured stock
emulation. The depth grid and 16-sample long exposure are approximations. Shader
coverage is the named fixture, not every shader pack. Hard crashes during writes
and an actual restart halfway through chemistry processing still need stress tests.

## Historical 0.6.0 baseline

The following records the previous release; tank return-on-close and contact-sheet
printing behavior below were superseded by 0.6.1.

## Tested

GitHub Actions builds version-specific Fabric JARs and runs **49 JUnit tests**,
plus the actual Minecraft client and integrated single-player server. The gameplay
revision passed on **1.21.10, 1.21.11, 26.1.2, 26.2 and 26.3** in
[run 36338689020](https://github.com/nobothehobo/Candid/actions/runs/36338689020),
code commit `c3396f455d8116813cf083315313995e36351ca5`.
The same run passed the 1.21.10 Iris/Sodium capture fixture. The same acceptance run includes HUD-free animation screenshots, a visible mounted
camera, and exactly one returned camera when the tripod is broken. Only passing
targets produce downloads.

26.3 also passed the real client workflow after installing EGL/software graphics
dependencies in CI. Physical Steam Deck testing remains manual.

This includes selected focus plus near/far depth-of-field limits, an independently
pivoting advance lever with skinned-hand motion, seven additional core tests, and a
winding screenshot/assertion in the real client workflow. Screenshots were inspected
at 854 × 480 on 1.21.10 and 26.3, including the hand anchored to the rotating lever
grip and unobstructed near/far scale. All five final JARs were downloaded, checked
for ZIP integrity, exact version metadata, included winding assets/classes and
absence of the game-test class.

The integrated test covers:

- Server-acknowledged loading and winding; screenshot of the camera back and skinned hands.
- Physical lever advance in the finder, retaining exactly 35 remaining frames after winding.
- Reference depth-of-field limits, aperture/lens changes, infinity, malformed optics,
  agreement with image blur, and bounded lever stroke/return.
- Firing through the actual Space-key handler, releasing jump input, keeping the
  capture screen open until completion, and consuming exactly one exposure.
- Rewind and cartridge-removal screenshots, early skipping of unloading, partial-roll reload, retained frame count and custody.
- Nonblank map pixels and a decodable, persisted **504 × 336 full-color PNG**.
- Tank slot transfer, exactly one chemistry consumption, repeated Start rejection,
  safe return of the roll on close, and the actual 20-second processing delay.
- Carried rolls complete without the basin; using film on terrain opens its contact sheet.
- Free previews allocate no map IDs and consume no photographic paper.
- PNG export with bytes exactly equal to the stored scan.
- Two normal prints consuming exactly two sheets; locked map pixels match the negative.
- Enlarger input slots, four-map matted prints, and reuse of map IDs for repeat copies.
- Lens attachment, distinct mounted barrel references, and return of the previous lens.
- Real stack transfer into the tripod, paired remote release, saved pan/tilt,
  actual viewpoint at the tripod head, and at least one second spent on a
  one-second exposure before the frame is consumed.
- Live scene meter changing by more than seven EV between noon and midnight.
- Save/leave/reopen preserving rolls, normal maps, large-print tiles and scan files.

Unit coverage includes EV/ISO/aperture/shutter relationships, sun versus shade,
weather, material reflectance, long shutter durations, focus-plane sharpness,
off-plane detail blur, all-stock exposure monotonicity, visibly separated ±2 EV
results, monochrome neutrality, film capacity, custody, duplicate frames,
development transitions, print validation, serialization/corruption, ordered writes,
legacy frame compatibility, scan persistence, safe/repeatable export paths and
mirrored crafting-recipe collisions.

Asset validation parses **129 resource JSON files**, checks mounted-lens model references
and normalized UVs, and covers **19 recipe definitions**. The repository retains
**39 editable Blockbench sources**. Test classes and shader-test libraries are not
bundled in release JARs.

## Shader test

The optional second CI job loads **Iris 1.9.7 + Sodium 0.7.3 for 1.21.10** and a
small original test shader. It checks that Iris reports an active pack and that
the shader's deliberate color change remains in the saved PNG. This tests the
capture path rather than assuming that framebuffer capture retains shader effects.
The shader workflow passed on September 26, 2026 in
[run 36269958324](https://github.com/nobothehobo/Candid/actions/runs/36269958324).
Screenshots confirm textured world geometry in the resulting scan. The deliberate
red test tint belongs only to this fixture; it is not a shipped Candid film effect.

## Fixes discovered during verification

- Loading initially submitted models but cancelled the renderer's deferred draw
  flush. The override now replaces the vanilla arm pass and preserves that flush.
  Screenshot review confirms that the camera, open back, cartridge and arms render.
- Iris' isolated development test needed its actual runtime shader directory and
  bundled parser libraries, plus textured geometry passes in the test shader.
  These changes affect only the optional test harness.
- The 28 mm and 35 mm recipes were mirrored equivalents. The 35 mm arrangement
  is now distinct; an automated check protects all shaped recipes against this.
- Shutter key release and an input-blocking capture screen prevent jump leakage.
- Load/unload is dispatched once on screen entry, independent of animation completion.
- The tripod lens viewpoint is raised/offset forward to avoid photographing its head.
- Exposure warning placement no longer overlaps the meter at the tested GUI size.

## Partially tested / limitations

- Film profiles are original approximations, not measured Kodak colorimetric models.
- Vanilla maps still have a limited palette/resolution. Full-color scans and exports
  preserve more detail; a normal item-frame print cannot display arbitrary RGB pixels.
- DOF uses a coarse depth grid; it does not simulate optical bokeh, reflections or
  live finder blur. Long exposures use at most 16 samples, not continuous integration.
- Loading uses staged model poses and skinned arms, not articulated fingers or cloth/film physics.
- No physical Steam Deck/Steam Input test, subjective audio listening test, or GPU
  performance benchmark was possible here. CI's audio/narrator-device warnings are expected.
- Individual third-party shader packs, their auto-exposure/DOF and resource-pack
  combinations require manual testing. The Iris fixture is not coverage of every pack.
- Large archives, malicious-client moderation and transactional hard-crash recovery
  remain public-server hardening work. Keep world backups.
- Export is a local PNG file for sharing through your own apps; no automatic social posting.
- Dodging/burning and further darkroom machinery are deferred as requested.

## Manual acceptance on Steam Deck

- [ ] Back up the existing world, replace its Candid JAR with the matching 0.6 build and Fabric API, and open it.
- [ ] Craft the camera, film, developer, tank, enlarger, paper and guide in survival.
- [ ] Use only the Field Guide to load, expose, develop, preview and print.
- [ ] Fire repeatedly with A; verify no jump and no duplicate action from Steam Input.
- [ ] Load/unload through the labeled button and Y; skip both animations early.
- [ ] Check skin/arm alignment while loading at different FOVs and GUI scales.
- [ ] Wind with R / X; check lever/hand contact and return. Shoot as soon as READY.
- [ ] Compare noon, sunset, night, rain, indoor shade and torch-lit scenes.
- [ ] Photograph the same subject at −2, −1, 0, +1 and +2 EV; compare free scans.
- [ ] Compare 28/35/50/90 mm framing and near/far focus at f/2 and f/16.
- [ ] Mount the actual camera, adjust its angle, pair a release, and fire from a distance.
- [ ] Take 1/8/30-second night shots and cancel one without spending film.
- [ ] Retrieve the camera, then remount and break the stand: exactly one camera returns.
- [ ] Close the tank mid-development, reopen/restart, and finish the same roll.
- [ ] Preview without paper, print repeated copies, and assemble a matted four-map display.
- [ ] Use a held print, Export PNG, and share the saved file from Desktop Mode.
- [ ] Repeat with your preferred shader pack; compare the finder composition and scan.
- [ ] Listen to shutter/wind/latch balance on the Deck's speakers and headphones.

## Exposure audit

ISO 800 at f/4, 1/1000 has the same metered offset as ISO 100 at f/4, 1/125.
All seven profiles preserve increasing middle-grey output from −2 through +2 EV,
retain highlight separation at +2 EV, and keep ±0.5 EV middle grey usable.
These are mathematical/image-response checks, not measured manufacturer colorimetry.
Shader auto-exposure, white balance and tone mapping remain baked into the source image.

## Performance bounds

Only one capture is active per client. Focus sampling is limited to 64 rays per tick;
the first framebuffer capture no longer waits for all 672 depth samples. Pure film
processing runs on one worker. Long exposures use at most 16 samples. Remote use never
loads chunks. Tripods add block entities, not display entities. These are enforced
work limits; no physical Steam Deck FPS or GPU benchmark is claimed.
