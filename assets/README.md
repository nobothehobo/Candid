# Editable Lumen artwork in Candid

The camera and developing tank are original Lumen35 geometry and a hand-authored
128px pixel atlas, imported from Lumen35 commit e7bc1d64c26baf198232b30f1641bbdcfb0319ae.
Open the .bbmodel sources in Blockbench; their embedded texture remains editable.
Run `python3 tools/export_models.py` to export the Java models. It converts
Blockbench's pixel UVs to Minecraft's normalized 0–16 coordinates; do not copy
the 128px atlas coordinates directly into a Java model.
The existing candid:camera and candid:darkroom_basin registry IDs are unchanged.
No third-party camera geometry, film logos or packaging is included.

Version 0.5 adds original enlarger, tripod and interchangeable-lens models, four
open-back/loading camera poses and a film-loading cartridge. The same exporter
recreates these Java models from editable sources. Camera poses include the
film chamber, take-up spool and perforated leader. The in-world loading renderer
uses the current player's skin for both arms; no prepainted generic hand texture
replaces the player's appearance.

The actual shutter audio is now an edited CC0 recording, with retained source in
`assets/audio` and provenance in `SOUND_ASSETS.md`. Other mechanical foley is original.
