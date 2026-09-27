"""Derive editable mounted-lens variants from the original camera artwork."""
import copy,json
from pathlib import Path
from export_models import export
ROOT=Path(__file__).resolve().parents[1]
# Axial extension from the fixed mount, with a modest increase in barrel diameter.
PROFILES={28:(.72,1.06),35:(1,1),50:(1.5,1.08),90:(2.55,1.17)}
for stage in ['', '_loading_1','_loading_2','_loading_3','_loading_4']:
    base=json.loads((ROOT/f'assets/blockbench/camera{stage}.bbmodel').read_text())
    for mm,(length,radius) in PROFILES.items():
        model=copy.deepcopy(base)
        for e in model['elements']:
            if any(e['name'].startswith(n) for n in ('Focus knurl','Focus ring','Aperture ring','Silver lens rim','Lens glass','Lens glint')):
                for key in ['from','to','origin']:
                    if key not in e:continue
                    x,y,z=e[key];e[key]=[7+(x-7)*radius,7.5+(y-7.5)*radius,4.9+(z-4.9)*length]
        name=f'camera{stage}_{mm}';model['name']=f'Candid {mm}mm{stage.replace("_"," ")}'
        (ROOT/f'assets/blockbench/{name}.bbmodel').write_text(json.dumps(model,separators=(',',':'))+'\n')
        export(name,f'item/{name}')
        (ROOT/f'src/main/resources/assets/candid/items/{name}.json').write_text(json.dumps({'model':{'type':'minecraft:model','model':f'candid:item/{name}'}})+'\n')
