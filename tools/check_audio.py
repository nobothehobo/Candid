"""Decode shipped camera sounds: an OGG header alone cannot detect silent exports."""
from pathlib import Path
import array
import math
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
for path in sorted((root / 'src/main/resources/assets/candid/sounds/camera').glob('*.ogg')):
    raw = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(path), '-f', 'f32le', '-ac', '1', '-ar', '44100', '-'])
    samples = array.array('f', raw)
    if sys.byteorder != 'little':
        samples.byteswap()
    assert samples, f'{path.name}: empty sound'
    peak = max(abs(x) for x in samples)
    rms = math.sqrt(sum(x*x for x in samples) / len(samples))
    assert peak > .02 and rms > .002, f'{path.name}: silent/inaudible export (peak={peak}, rms={rms})'
    if path.name == 'shutter.ogg':
        onset = next(i for i, x in enumerate(samples) if abs(x) > .02) / 44100
        assert onset < .05, f'Shutter has {onset:.3f}s leading silence'
    print(f'{path.name}: {len(samples)/44100:.3f}s, peak {20*math.log10(peak):.1f} dBFS, RMS {20*math.log10(rms):.1f} dBFS')
