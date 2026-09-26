# Future StateCraft integration — not performed

This branch targets Fabric 1.21.10, not StateCraft's unknown server baseline.
Do not install this JAR on Paper or assume a Bedrock client can run it.

## Reusable components

The Java exposure calculations, luminance aggregation, film response, roll-state
transitions, frame metadata and their unit tests have no Minecraft client
dependency. The original camera/tank geometry, pixel atlas and Blockbench
sources can be adapted to the target resource pack.

## Adapter work required

Inspect StateCraft's actual versions, conventions and designer branch first.
Replace Fabric item registration, data components, recipe JSON, GUI screens,
network payloads, lifecycle hooks and per-world file storage with its established
item/Nexo, recipe-browser, persistence, permissions, configuration and UI systems.
Film IDs and saved metadata need an explicit migration if existing data is imported.

The **framebuffer capture is client-only** and cannot be reused by a server-only
Paper deployment. That deployment needs Lumen35's incremental server-side scene
renderer (or another honest server-known-world renderer), with separate image
quality/performance tests. Likewise, scene sampling must use safe Paper world APIs.

Audit Geyser/Bedrock controls, custom models, maps, crafting and development on
the real server. Add transactional inventory/storage handling, quotas and cache
eviction before treating this prototype as a public-server economy system.
No StateCraft repository or production branch was accessed by this integration.

Version 0.5 also separates reusable optics/DOF and PNG export validation from
Fabric screens and item controls. Full-color PNG sidecars can be retained behind
StateCraft's storage adapter. The physical loading hands, client FOV changes and
Iris framebuffer path require a modded client and cannot be promised to Bedrock
or a server-only deployment. Server-side photography needs its own renderer and
resource-pack-compatible feedback. PNG sharing could be exposed by a separately
reviewed server download mechanism; this version deliberately exports locally.
