#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Reset timestamps before fading: trimming without asetpts produced a silent export.
ffmpeg -v error -y -i assets/audio/drewtait-676375-preview.mp3 \
  -af 'atrim=start=2.19:end=2.52,asetpts=PTS-STARTPTS,highpass=f=70,lowpass=f=10500,afade=t=in:d=0.002,afade=t=out:st=0.25:d=0.08,alimiter=limit=0.89:level=false' \
  -ac 1 -c:a libvorbis -q:a 6 src/main/resources/assets/candid/sounds/camera/shutter.ogg
# Replace the damaged legacy leader-friction file with original, quiet paper foley.
ffmpeg -v error -y -f lavfi -i 'anoisesrc=color=brown:amplitude=0.4:duration=0.45:sample_rate=44100:seed=35' \
  -af 'highpass=f=700,lowpass=f=5000,afade=t=in:d=0.02,afade=t=out:st=0.30:d=0.15' \
  -ac 1 -c:a libvorbis -q:a 6 src/main/resources/assets/candid/sounds/camera/film_load.ogg
python3 tools/check_audio.py
