# Candid camera audio

Candid 0.5 uses a real recorded film-camera shutter from **drewtait**, “Film Camera Click Shutter” (Minolta XG-1), released under **CC0**.

- Source: https://freesound.org/people/drewtait/sounds/676375/
- License: https://creativecommons.org/publicdomain/zero/1.0/
- Public preview source used: https://cdn.freesound.org/previews/676/676375_14682229-hq.mp3
- Retrieved September 21, 2026. No account-only download was accessed.
- Retained source: `assets/audio/drewtait-676375-preview.mp3`.
- 0.6.1 edit: extract 2.19–2.52 seconds and RESET audio timestamps before filters;
  mono, high-pass 70 Hz, low-pass 10.5 kHz, 2 ms fade-in, 80 ms tail fade,
  peak limiter at 0.89; encode Vorbis quality 6. `bash tools/rebuild_shutter.sh`
  reproduces the edit. The old shipped OGG decoded to silence (-91 dBFS).
- The repaired shutter measures approximately -1.3 dBFS peak / -21 dBFS RMS.
  `tools/check_audio.py` decodes every camera sound and rejects silent, corrupt or
  delayed-onset shutter exports in CI. The damaged leader-friction OGG was also
  replaced with deterministic original filtered-noise foley (MIT).
- Shipped result: `assets/candid/sounds/camera/shutter.ogg`.

This is a real SLR recording, not a recording of a particular rangefinder. It supplies the requested mechanical film-shutter click without suggesting model-exact acoustics. Advance, leader friction and back-latch sounds remain the original Candid synthesized foley under MIT. Audible balance on physical Steam Deck speakers remains a manual check.

The CC0 sample is independently licensed; the MIT repository license does not change its CC0 provenance.
