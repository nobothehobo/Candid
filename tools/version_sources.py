"""Create a target-specific source tree from shared sources and narrow adapter overlays."""
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def transform(source, target):
    if target != "1.21.10":
        source = source.replace("ResourceLocation", "Identifier").replace("net.minecraft.Util", "net.minecraft.util.Util")
    if target == "1.21.11":
        source = source.replace(".submitOutline(", ".renderOutline(")
    if target != "1.21.10":
        source = source.replace(".getMainCamera().getPosition()", ".getMainCamera().position()")
    if target.startswith("26."):
        renames = {
            "GuiGraphics": "GuiGraphicsExtractor",
            "void render(": "void extractRenderState(",
            "super.render(": "super.extractRenderState(",
            "renderBackground(": "extractBackground(",
            ".drawCenteredString(": ".centeredText(",
            ".drawString(": ".text(",
            ".renderItem(": ".item(",
            ".submitOutline(": ".outline(",
            "ClickType": "ContainerInput",
            ".getDayTime()": ".getOverworldClockTime()",
            ".playC2S()": ".serverboundPlay()",
            ".playS2C()": ".clientboundPlay()",
            ".modifyEntriesEvent(": ".modifyOutputEvent(",
            "net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup": "net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab",
            "net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents": "net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents",
            "FabricItemGroup": "FabricCreativeModeTab",
            "ItemGroupEvents": "CreativeModeTabEvents",
        }
        for old, new in renames.items():
            source = source.replace(old, new)
        # Overlay messages replaced the former message+boolean overload. Match
        # only that call, with balanced parentheses so nested Components survive.
        needle = ".displayClientMessage("
        while needle in source:
            start = source.index(needle)
            cursor = start + len(needle)
            depth = 1
            while depth:
                depth += (source[cursor] == "(") - (source[cursor] == ")")
                cursor += 1
            args = source[start + len(needle):cursor - 1]
            component, overlay = args.rsplit(",", 1)
            assert overlay.strip() == "true", "Only overlay messages are used by Candid"
            source = source[:start] + ".sendOverlayMessage(" + component + ")" + source[cursor:]
    if target in ("26.2", "26.3"):
        source = re.sub(r"(Minecraft\.getInstance\(\)|minecraft|mc|client|c)\.setScreen\(", r"\1.gui.setScreen(", source)
        source = re.sub(r"\.screen\b(?!\()", ".gui.screen()", source)
        source = source.replace(".getMainRenderTarget()", ".gameRenderer.mainRenderTarget()")
        source = source.replace(".getMainCamera()", ".mainCamera()")
        for color in ("LIME", "GRAY"):
            source = source.replace(f"Items.{color}_DYE", f"Items.DYE.{color.lower()}()")
    if target == "26.3":
        source = source.replace("net.minecraft.util.Util.getPlatform().openPath(", "com.mojang.blaze3d.Blaze3D.openPath(")
        source = source.replace("p.drop(s,false)", "p.drop(s,false,net.minecraft.util.Prediction.SERVER_ONLY)")
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
