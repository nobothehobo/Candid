# Candid modern Fabric compatibility

Candid 0.6 is a set of version-specific Fabric mods. It retains the 0.5 camera,
seven film stocks, exposure and focus, 36-frame rolls, animated loading, development
slots, enlarger, free previews, full-color scans, PNG sharing, matted map prints,
interchangeable lenses and tripod exposures. No separate Paper server is needed
for single-player play. The older 0.5 download remains available.

## Why separate files?

Minecraft 26.1 changed to unobfuscated code and Java 25. Newer releases also
changed screen/rendering APIs; 26.3 replaced GLFW input with SDL. A single JAR
with an open-ended Minecraft version requirement would not make those APIs
compatible. Each build targets one exact stable release and uses the matching
Fabric API. See the version table in the root README.

Only targets with a green build **and** client workflow test are release-ready.
The GitHub Actions matrix creates a downloadable JAR after both succeed.
Snapshots and unlisted future versions are not claimed supported.

## Prism / Steam Deck

1. Back up the instance and world. To try a newer Minecraft release, duplicate
   the Prism instance, then upgrade the copy. Vanilla world upgrades may be irreversible.
2. Select the target Minecraft release and Fabric Loader 0.19.5 or newer.
3. Select Java 21 for 1.21.10/1.21.11, or Java 25 for 26.x.
4. Install the matching Fabric API from the README table.
5. Remove the previous Candid JAR from this instance and add the matching 0.6 file.
6. Launch and use the Candid Field Guide. Models, sounds and recipes are bundled.

Use a gamepad layout or a keyboard/mouse Steam Input layout, avoiding duplicate
physical-button mappings through both systems. On 26.3, native gamepads are read
through SDL. Camera input remains gated by the open camera screen; gameplay
buttons are released while the shutter captures. Physical Steam Deck testing
is a separate manual acceptance step.

## Existing worlds and photographs

Registry IDs, custom-data keys, roll schema and scan paths remain unchanged.
Candid's files stay under `<world>/candid/`, while ordinary map data follows
Minecraft's world storage. Back up the **entire world**, not just its Candid folder.
Existing negatives retain their frames and map references; a legacy negative
without a full-color PNG still offers its stored map proof.

World reopen tests verify each target's own persistence. Cross-version upgrades
of existing worlds must follow Mojang's supported upgrade path. Do not downgrade
a world. This milestone does not claim that every older modpack or shader pack
can migrate alongside Candid unchanged.

## Shader and graphics compatibility

Candid captures Minecraft's completed render target and uses its image/texture
APIs instead of raw OpenGL readback. This keeps the integration separate from
a specific rendering backend. Iris, Sodium and shader packs must independently
support the chosen Minecraft release. See the test report for tested combinations.
A successful vanilla workflow is not proof of compatibility with every shader pack
or the experimental Vulkan renderer.

## Build architecture

- `src/main`: shared production source and resources.
- `src/test`, `src/gametest`: shared core tests and real client workflow.
- `src/versions/<layer>/<source set>`: small adapters overriding shared classes
  when an API changes structurally, such as 26.3 hand rendering and SDL input.
- `gradle/targets.json`: pinned Minecraft, Java and Fabric API combinations.
- `tools/version_sources.py`: deterministic generation under `build/<target>`;
  mechanical API renames and layered adapters never modify checked-in sources.
- `build.gradle`: remapped Loom for 1.21.x.
- `build-modern.gradle`: unobfuscated Loom for 26.x.
- `gradle/candid.gradle`: common build, resources, tests and artifact naming.

Java 25 can build the entire matrix; Java 21 builds only the 1.21.x targets.
Python 3 is required for source preparation. Use the checked-in Gradle wrapper:

```sh
./gradlew build -Ptarget=26.3
xvfb-run -a ./gradlew runClientGameTest -Ptarget=26.3
```

Use `runClient` for an interactive development client. Replace the target with
any key in `gradle/targets.json`. Default target is 1.21.10. The installable JAR
is in `build/<target>/libs/`; the `-sources.jar` is for development only.

For a future Minecraft release, add a pinned target and CI entry, adapt only the
changed APIs, and pass both the unit suite and full camera/darkroom/reopen workflow
before releasing. Do not broaden `fabric.mod.json` optimistically.
