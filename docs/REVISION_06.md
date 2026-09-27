# 0.6 handling revision

Implementation in progress on feature/modern-fabric; this document is not a test-pass claim.

- Tripods own a persistent camera stack and save independent yaw/pitch. Use to compose;
  crouch-use to retrieve. Breaking the stand returns the camera via block-entity removal.
- Cable Release: stone button, copper ingot, string in a vertical column. Use on an
  occupied stand to pair; use within 32 blocks in the same loaded dimension to compose
  and shoot. No forced chunk loading or display entities.
- Carried processing rolls complete automatically. Film can open its contact sheet
  against ordinary terrain; a basin visit is not required after processing.
- Mounted 28/35/50/90 mm barrels differ in length and diameter. Editable Blockbench
  variants also cover the animated opening-back stages.
- Shutter click occurs at accepted release; bounded focus sampling no longer delays
  the first framebuffer capture. Image processing remains asynchronous.
- Unloading shows release, rewind, cartridge removal and closure with the player's skin.
  Entering the handheld finder has a 12-tick raise gesture; repeated shots do not replay it.

Film profiles remain original, stock-inspired interpretations, not measured manufacturer
emulations. ISO changes metered exposure by log2(ISO/100); changing film does not increase
source framebuffer dynamic range. Shader tone mapping and auto-exposure are baked into
captures and cannot be reversed into scene-linear RAW data. Grain, shoulder and color
character are applied to that rendered image; a blown shader highlight cannot be recovered.

Validation to complete: five-version CI, mounted-camera saves and removal, direct negative
use, physical controller feel, visual arm/barrel alignment, and third-party shader packs.
