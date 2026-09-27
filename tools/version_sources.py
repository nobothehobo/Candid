"""Create a target-specific source tree from shared sources and narrow adapter overlays."""
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def transform(source, target):
    if target.startswith("26."):
        renames = {
            "ResourceLocation": "Identifier",
            "net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup": "net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab",
            "net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents": "net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents",
            "FabricItemGroup": "FabricCreativeModeTab",
            "ItemGroupEvents": "CreativeModeTabEvents",
        }
        for old, new in renames.items():
            source = source.replace(old, new)
    return source

def generate(target, output):
    definition = json.loads((ROOT / "gradle/targets.json").read_text())[target]
    if output.exists():
        shutil.rmtree(output)
    for source_set in ("main", "test", "gametest"):
        files = {}
        roots = [ROOT / "src" / source_set / "java"]
        roots += [ROOT / "src/versions" / layer / source_set for layer in definition["layers"]]
        for root in roots:
            if root.exists():
                files.update({p.relative_to(root): p for p in root.rglob("*.java")})
        for relative, path in files.items():
            dest = output / source_set / relative
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_text(transform(path.read_text(), target))

if __name__ == "__main__":
    generate(sys.argv[1], Path(sys.argv[2]))
