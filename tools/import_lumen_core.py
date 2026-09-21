"""One-time, auditable import of the platform-independent Lumen35 exposure core."""
from pathlib import Path
import sys
source=Path(sys.argv[1])
target=Path(__file__).resolve().parents[1]/'src/main/java/com/nobothehobo/candid/core'
target.mkdir(parents=True,exist_ok=True)
for name in ('Exposure.java','LightMeter.java'):
    text=(source/'src/main/java/dev/lumen35/core'/name).read_text()
    text=text.replace('package dev.lumen35.core;','package com.nobothehobo.candid.core;')
    (target/name).write_text(text)
