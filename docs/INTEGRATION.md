# Candid integration and upgrade notes

The 0.6 compatibility line has separate builds listed in [COMPATIBILITY.md](COMPATIBILITY.md).
The original baseline remains Minecraft Java 1.21.10 / Java 21; modern 26.x
builds use Java 25. Shared tools: Fabric Loader 0.19.5+, Loom 1.17.21, Gradle 9.5.1.
The Fabric JAR is not a Paper/Nexo plugin and cannot run on Bedrock.
Back up worlds; replace the older Candid JAR rather than installing both.

## Architecture and persistence

The imported platform-independent Lumen35 exposure/meter logic remains under
`com.nobothehobo.candid.core`; the original source provenance is Lumen35 commit
`e7bc1d64c26baf198232b30f1641bbdcfb0319ae`. Roll state, optics, depth-of-field
processing and export validation are independently testable. Fabric registries,
networking, screens, world sampling and lifecycle hooks surround that core.

- Roll records: `<world>/candid/rolls/<uuid>.json`, with previous-checkpoint `.bak`.
- Full-color scans: `<world>/candid/scans/<frame-uuid>.png` (504 × 336).
- Vanilla maps: Minecraft-managed map data (layout varies by game version). Preserve the whole world when moving saves.
- Player exports: `<Minecraft instance>/candid-exports/<frame-uuid>.png`.
- Items carry compact identifiers and settings, not embedded scans.
- Reprints reuse frame map IDs, including the four large-print tiles.
- Old JSON records remain readable: missing high-resolution/scans/tiles fields
  fall back to the available map proof. Older loaded camera counts are preserved.
- Legacy loose negatives retain the earlier compatibility path. New prints add
  roll/frame references so Use can open their full-color preview.
- Development records its finish time once. Restart/closing the station does not
  consume chemistry again. Development ends further exposure of the roll.

The authoritative ledger checks camera custody, duplicate frame IDs, roll capacity,
development state, frame validity and paper before printing. Station slots validate
inputs and return items on close. Inventory saves and external files are not a single
atomic transaction: hard crashes can require backup recovery. This is not yet certified
for a public-server economy. A modified multiplayer client can submit image pixels;
image moderation, archive quotas and stronger transaction recovery are future work.

## Rendering and limits

The same 3:2 crop defines finder and capture. Lens field of view uses a 36 × 24 mm
film reference. Full-color scans avoid the map palette; ordinary prints are 128 × 128
maps, and large displays use four 128 maps from a 256 × 256 matted proof. Existing
low-resolution negatives cannot regain missing detail.

Depth of field uses a bounded 32 × 21 scene-depth grid and thin-lens approximation.
It affects the finished photograph, not a continuously blurred optical viewfinder.
Thin objects, water, glass, reflections and shader geometry can disagree with depth
samples. The focus-distance indicator meters the center block surface. Long exposures
average 2–16 actual samples in linear color over the selected duration; trails are
approximate, not a continuous simulation. A mounted camera remains in hand.

The meter uses reflected material brightness, directional sun/shade, ambient skylight,
block light, time and weather. It is deliberately calibrated for playable Sunny-16-like
behavior, not laboratory photometry. Shader auto-exposure and the game's brightness
settings change the rendered pixels independently of this world-based meter.

The real shutter sample is CC0; see SOUND_ASSETS.md. Loading models animate in world
with the player's skin while the server alone performs inventory changes. These are
staged poses rather than physically simulated finger/film contact.

## Performance protections

- One active capture per client, one background film processor, 45-second timeout.
- Nine metering rays, with up to nine sun-visibility rays, at four updates per second.
- Depth sampling: at most 64 rays per tick, 32 × 21 total, 96-block distance bound;
  nearby entity depth uses a capped list. No per-frame world raycast for each image pixel.
- Framebuffer readback/downsampling stays on the render thread; film, DOF, PNG encoding
  and palette work run off-thread. There is no network upload per video frame.
- At most 16 temporal samples for long exposures, regardless of shutter duration.
- One bounded incoming scan assembly per player, 10-second expiry; map proof 64 KiB,
  PNG at most 512 KiB in 16 KiB chunks, validated header/dimensions and frame UUID.
- Ordered asynchronous roll/image writes, coalesced checkpoints and shutdown flush.
- No new per-photo display entities: display uses ordinary Minecraft item frames.

The archive currently stays in memory while a world is open. Large public collections
need eviction and quotas. CI's software-rendered timings are regression evidence,
not a Steam Deck FPS benchmark. Test long-session frame pacing on target hardware.

## Tests

`bash gradlew build compileGametestJava`

`xvfb-run -a bash gradlew runClientGameTest`

For the optional shader fixture:

```
python3 tools/setup_shader_test.py
CANDID_SHADER_TEST=true xvfb-run -a bash gradlew runClientGameTest
```

The fixture downloads checksum-pinned Iris 1.9.7 and Sodium 0.7.3 for 1.21.10,
installs a small original test shader in the isolated game test, and verifies
shader-colored pixels in the persisted scan. These dependencies and test classes
are excluded from the release mod. The async-network test setting handles 1.21.10
login correctly; assertions await real state changes.

The in-game guide reads diagrams from the same 18 JSON recipe definitions as the
mod. A data pack overriding recipes may differ from the shipped guide.
Dodging/burning and an expanded darkroom remain deliberately deferred.
