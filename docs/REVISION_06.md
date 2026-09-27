# 0.6 handling revision

Implemented on feature/modern-fabric. See TEST_REPORT.md for target-specific validation and remaining limits.

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
- The winding lever is a separate rotating mesh, with a 70-degree outward stroke and
  spring return coordinated with the player's skinned hand. Manual winding keeps the
  finder open; a ready shutter can interrupt the cosmetic motion. Mounted remote
  winding does not conjure a handheld camera or hand next to a distant stand.
- The finder displays selected focus and approximate near/far sharpness limits for
  the current lens and aperture. The center indicator uses the same limits. These
  use a 0.03 mm circle of confusion on a 35 mm frame; actual perceived sharpness also
  depends on output size. The existing infinity focus stop now uses the thin-lens
  infinity limit in both range calculation and image blur.

Film profiles remain original, stock-inspired interpretations, not measured manufacturer
emulations. ISO changes metered exposure by log2(ISO/100); changing film does not increase
source framebuffer dynamic range. Shader tone mapping and auto-exposure are baked into
captures and cannot be reversed into scene-linear RAW data. Grain, shoulder and color
character are applied to that rendered image; a blown shader highlight cannot be recovered.

Validated including the focus/lever follow-up: 49 unit tests, direct negative use,
five-version in-game workflow, remote aiming and mounted-camera saves.
Release checks also passed camera removal and animation screenshots. Remaining manual checks include
physical controller feel, arm/barrel alignment at varied FOVs, and third-party shader packs.
