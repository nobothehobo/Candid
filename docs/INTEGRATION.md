# Candid 0.4 — Lumen35 integration

This is the existing Candid Fabric mod, upgraded in place for Minecraft Java
1.21.10, Java 21, Fabric Loader 0.19.5+, and Fabric API 0.138.4+1.21.10.
Do not install the Paper Lumen35 plugin in a Fabric instance. Keep one Candid
JAR in `mods`; remove the older Candid JAR when updating. Back up existing worlds.

## What was reused

The platform-independent Lumen35 `Exposure` and `LightMeter` calculations were
imported into `com.nobothehobo.candid.core`. Their source provenance is the local
Lumen35 commit `e7bc1d64c26baf198232b30f1641bbdcfb0319ae`. Candid retains its own
registry IDs, original models, sound assets, controller screens and client
framebuffer capture. There is no Paper, Nexo, SQLite or server plugin dependency.

## Gameplay

1. Craft camera and film. All seven stocks provide one complete 36-shot roll.
2. Crouch + Use the camera. Select FILM and operate the control to load.
3. Use to enter the viewfinder. The bright 3:2 boundary is the captured region.
4. Set aperture/shutter; aim near zero EV. ISO comes from the loaded film.
5. Shoot, then manually wind for the next frame.
6. Camera controls: **Y / U / Rewind button** returns full or partial film.
7. Use that roll on the Darkroom Basin with one Developer Chemistry.
8. After 20 seconds, use the roll to open its contact sheet.
9. Click a frame to consume one Photo Paper and receive a locked map print.
10. Repeat printing from the same negative or use the Enlarger for duplicates.

The Field Guide includes instructions and all thirteen recipes, with recipe
diagrams read from the same JSON files that register the shipped recipes.
Data packs that override these recipes may differ from the shipped guide.

## Persistence and compatibility

- New roll records live in `<world>/candid/rolls/<uuid>.json`, with a `.bak` of
  the previous checkpoint. Preserve the entire world, including `data/map_*.dat`.
- Items contain compact UUIDs. Frame palette bytes and metadata live in world
  storage, not huge item tags. Repeated prints share a map ID.
- Legacy loaded cameras keep their remaining exposure count on first use.
  Their old loose negatives/prints remain on the legacy basin/enlarger path.
- Existing negatives already inverted through the old map palette cannot be
  reconstructed exactly. New rolls retain the original positive palette bytes.
- Partial rolls may be rewound and reloaded with the leader retained as a
  gameplay convenience. Development ends further exposure of that roll.
- Client and server must both use the same Candid version for multiplayer.

## Accuracy and limits

The viewfinder and capture share a centered 3:2 crop; the map adds a paper border
instead of stretching a widescreen image into a square. The center patch is a
metering aid, not a simulated optical focusing mechanism. Aperture and shutter
affect exposure; depth of field and motion blur are not simulated. Capture uses
the game's rendered image, so shaders, brightness settings and resource packs
can affect its appearance. It is not a physical scene-radiance measurement.

Golden, Everyday, Portrait and Mono stocks are original profiles inspired by
published film characteristics; they are not measured Kodak LUTs. See
`FILM_REFERENCE.md` for primary references.

The persistent roll ledger rejects duplicate exposure IDs, wrong-camera custody,
over-capacity exposure, repeated development and printing without paper.
However, Minecraft inventory saves and external roll checkpoints are not one
atomic transaction. Hard crashes can require restoring a world backup. This
prototype is not certified for a public-server economy. A modded client can
submit arbitrary image pixels; multiplayer image moderation is separate work.

## Performance

One pending capture per client, a bounded 16,384-byte image payload, 700 ms
server capture spacing, and one background film processor. The meter samples
nine bounded 32-block rays at four updates per second. Pixel processing and
ordered roll-file writes run off-thread. Checkpoints coalesce changes once per
second; orderly shutdown flushes and waits for writes. No per-photo entities.
Archives remain in memory while the world is open; huge collections need cache
eviction and storage quotas before large-server use.

## Build and test

`bash gradlew build compileGametestJava` runs unit tests and builds the remapped
mod. `xvfb-run -a bash gradlew runClientGameTest` runs the real client/integrated
server workflow on Linux with a virtual display. These tests are in the separate
`gametest` source set, not the release mod.

This Work host's Loom socket-capability probe is restricted. Local validation
uses a build-only adapter that reports optional Unix sockets as unavailable;
the restriction itself is unchanged. GitHub Actions uses unmodified Loom
1.17.21 and the checked-in Gradle wrapper. No build adapter ships in the mod.

Manual acceptance still includes Steam Deck controls/Steam Input mappings,
different GUI scales, shader compatibility, photographs of moving entities,
and subjective film/model appearance. Automated screenshots and assertions do
not replace testing on the user's device.
