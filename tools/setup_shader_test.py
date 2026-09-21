"""Optional CI-only Iris/Sodium fixture. Nothing here is bundled in the mod."""
from pathlib import Path
import hashlib,subprocess
mods=Path('build/shader-test/dependencies');mods.mkdir(parents=True,exist_ok=True)
artifacts=[
 ('https://cdn.modrinth.com/data/YL57xq9U/versions/a98UkgML/iris-fabric-1.9.7%2Bmc1.21.10.jar','iris.jar','765837034be497b83ceef1a46dfdb305a9edf0c05762979d261b3917b6513ce25158d74252315523157bc02217354da36686479f0e09c1ae453a987fb9f8bfb3'),
 ('https://cdn.modrinth.com/data/AANobbMI/versions/sFfidWgd/sodium-fabric-0.7.3%2Bmc1.21.10.jar','sodium.jar','1cccdc75d972f5c176a488dcc84cce7320b608a2d105412f2847245affbf5aa22b1995eda392132453fa6e1a9154ac99a87acaf8d7989b9f1a23ac1059a93daf')]
for url,name,digest in artifacts:
 p=mods/name;subprocess.run(['curl','--fail','--location','--silent',url,'--output',str(p)],check=True)
 assert hashlib.sha512(p.read_bytes()).hexdigest()==digest,'Dependency checksum mismatch'
# Loom file dependencies do not automatically expose Iris' nested Java libraries.
# Fabric API is already supplied by this project's pinned dependency.
import zipfile
with zipfile.ZipFile(mods/'iris.jar') as archive:
    libs=mods/'libraries';libs.mkdir(exist_ok=True)
    for entry in archive.namelist():
        if entry.startswith('META-INF/jars/') and entry.endswith('.jar') and not Path(entry).name.startswith('fabric-'):
            (libs/Path(entry).name).write_bytes(archive.read(entry))
