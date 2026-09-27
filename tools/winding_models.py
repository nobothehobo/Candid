"""Split existing editable camera geometry at the advance lever's physical pivot.

Run after mounted_lenses.py. Rendering rotates this separate part continuously;
the original artwork and all four interchangeable barrels remain unchanged.
"""
import copy
import json
from pathlib import Path
from export_models import export

ROOT = Path(__file__).resolve().parents[1]
LEVER = {"Advance lever", "Lever grip"}

def part(source, name, lever):
    model = copy.deepcopy(source)
    model["name"] = name
    model["elements"] = [e for e in model["elements"] if (e["name"] in LEVER) == lever]
    assert model["elements"]
    model["outliner"] = [e["uuid"] for e in model["elements"]]
    (ROOT / f"assets/blockbench/{name}.bbmodel").write_text(json.dumps(model, separators=(',', ':'))+'\n')
    export(name, f"item/{name}")
    (ROOT / f"src/main/resources/assets/candid/items/{name}.json").write_text(json.dumps({"model":{"type":"minecraft:model","model":f"candid:item/{name}"}})+'\n')

for mm in (28, 35, 50, 90):
    source = json.loads((ROOT / f"assets/blockbench/camera_{mm}.bbmodel").read_text())
    part(source, f"camera_winding_{mm}", False)
    if mm == 35:
        part(source, "camera_advance_lever", True)
