"""Export continuous hinge/leader parts from the retained original Blockbench artwork."""
import copy,json
from pathlib import Path
from export_models import export
ROOT=Path(__file__).resolve().parents[1]
def write(source,name,predicate):
    model=copy.deepcopy(source);model['name']=name
    model['elements']=[e for e in model['elements'] if predicate(e['name'])]
    model['outliner']=[e['uuid'] for e in model['elements']]
    assert model['elements']
    (ROOT/f'assets/blockbench/{name}.bbmodel').write_text(json.dumps(model,separators=(',',':'))+'\n')
    export(name,f'item/{name}')
    (ROOT/f'src/main/resources/assets/candid/items/{name}.json').write_text(json.dumps({'model':{'type':'minecraft:model','model':f'candid:item/{name}'}})+'\n')
for mm in (28,35,50,90):
    model=json.loads((ROOT/f'assets/blockbench/camera_loading_1_{mm}.bbmodel').read_text())
    write(model,f'camera_open_{mm}',lambda n:n!='hinged camera back')
    if mm==35:write(model,'camera_back',lambda n:n=='hinged camera back')
model=json.loads((ROOT/'assets/blockbench/camera_loading_4.bbmodel').read_text())
write(model,'camera_loaded_cartridge',lambda n:n.startswith('film cartridge'))
write(model,'camera_leader',lambda n:n=='film leader' or 'sprocket' in n)
