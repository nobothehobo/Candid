# Candid 0.6.1 playtest fixes

Implemented; build and in-game verification pending when this file was first written.

- Repaired the silent shutter OGG (old peak -91 dBFS); reset timestamps before fading.
  Added decoded-audio regression checks, and repaired corrupt film-loading foley.
- Raise-to-eye shortened from 12 to 5 ticks, with eased motion. The back door now
  uses a continuous hinge transform; film leader length interpolates continuously.
  Winding follows the lever position with limited wrist rotation instead of rotating
  the whole forearm. Hands are separated/repositioned. Physical hands/camera are
  never rendered inside the finder when advancing with R / X.
- One HUD-clearing tick instead of two before capture. Bounded focus pre-sampling
  in the finder reuses only recent, matching camera poses. A brief 140 ms mirror-style
  blackout is rendered AFTER the last image readback, never inside the saved image.
  Finder returns while processing finishes; RECORDING indicates the one-job limit.
- First-person viewpoint is enforced during camera use, and the previous perspective
  is restored on exit. No third-person selfie captures.
- Developing Tank now has persistent real input slots. Closing leaves contents inside;
  processing rolls are locked until ready. Reopening finishes elapsed jobs from their
  saved timestamps. Breaking drops contents once. No hopper automation or extra tick loop.
- Contact-sheet left AND right clicks only preview. Printing is explicit at an enlarger.
- Mounted cameras have a generous, ray-tested interaction area above the stand's block.
  Client targeting is revalidated server-side against range, loaded chunks and walls.
- Paired cable Use fires; Crouch + Use opens composition. Terrain use is supported.
  Paired lore and status describe these controls; unwound/no-film feedback is explicit.
- Focus bars align inside the subject patch; nearer/farther instructions and PC/Deck
  focus controls are visible. This is a distance-driven alignment aid, not simulated
  optical split-image glass or live bokeh. The near/far depth-of-field scale remains.

All existing 0.6 worlds should be backed up before updating. Install matching 0.6.1
on both sides of Fabric multiplayer because the tripod-view packet gained a release flag.
The Paper/StateCraft migration is not part of this update.
