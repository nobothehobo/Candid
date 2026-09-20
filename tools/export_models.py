"""Export the editable Blockbench sources to normalized Minecraft Java UVs.

Blockbench stores atlas pixel coordinates. Java model UVs always use 0..16,
even when the texture is 128 pixels wide. No raster artwork is modified.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def export(name, destination):
    source = json.loads((ROOT / f"assets/blockbench/{name}.bbmodel").read_text())
    width, height = source["resolution"]["width"], source["resolution"]["height"]
    elements = []
    for cube in source["elements"]:
        element = {key: cube[key] for key in ("name", "from", "to")}
        element["faces"] = {}
        for side, face in cube["faces"].items():
            uv = [v * 16 / (width if i % 2 == 0 else height) for i, v in enumerate(face["uv"])]
            assert all(0 <= v <= 16 for v in uv), (name, side, uv)
            element["faces"][side] = {"uv": uv, "texture": "#0"}
        rotated = [(axis, angle) for axis, angle in zip("xyz", cube.get("rotation", [0, 0, 0])) if angle]
        if rotated:
            assert len(rotated) == 1, "Java cubes support one rotation axis"
            axis, angle = rotated[0]
            element["rotation"] = {"axis": axis, "angle": angle, "origin": cube["origin"], "rescale": False}
        elements.append(element)
    model = {
        "credit": "Original Lumen35 geometry, Candid integration",
        "gui_light": "front",
        "textures": {"0": "candid:item/atlas", "particle": "candid:item/atlas"},
        "elements": elements,
        "display": source["display"],
    }
    (ROOT / f"src/main/resources/assets/candid/models/{destination}.json").write_text(json.dumps(model, indent=2) + "\n")


if __name__ == "__main__":
    export("camera", "item/camera")
    export("tank", "block/darkroom_basin")
