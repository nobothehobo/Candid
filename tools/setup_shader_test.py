"""Optional CI-only Iris/Sodium fixture. Nothing here is bundled in the mod."""
from pathlib import Path
import hashlib,urllib.request,subprocess
root=Path('build/run/clientGameTest')
(root/'mods').mkdir(parents=True,exist_ok=True)
artifacts=[
 ('https://cdn.modrinth.com/data/YL57xq9U/versions/a98UkgML/iris-fabric-1.9.7%2Bmc1.21.10.jar','iris.jar','765837034be497b83ceef1a46dfdb305a9edf0c05762979d261b3917b6513ce25158d74252315523157bc02217354da36686479f0e09c1ae453a987fb9f8bfb3'),
 ('https://cdn.modrinth.com/data/AANobbMI/versions/sFfidWgd/sodium-fabric-0.7.3%2Bmc1.21.10.jar','sodium.jar','1cccdc75d972f5c176a488dcc84cce7320b608a2d105412f2847245affbf5aa22b1995eda392132453fa6e1a9154ac99a87acaf8d7989b9f1a23ac1059a93daf')]
for url,name,digest in artifacts:
 p=root/'mods'/name;subprocess.run(['curl','--fail','--location','--silent',url,'--output',str(p)],check=True)
 assert hashlib.sha512(p.read_bytes()).hexdigest()==digest,'Dependency checksum mismatch'
shader=root/'shaderpacks/Candid-Test/shaders';shader.mkdir(parents=True,exist_ok=True)
(shader/'final.vsh').write_text('#version 120\nvarying vec2 uv;\nvoid main(){gl_Position=ftransform();uv=gl_MultiTexCoord0.xy;}\n')
(shader/'final.fsh').write_text('#version 120\nuniform sampler2D colortex0;\nvarying vec2 uv;\nvoid main(){vec3 c=texture2D(colortex0,uv).rgb;gl_FragColor=vec4(c*vec3(1.0,0.15,0.15),1.0);}\n')
(root/'config').mkdir(exist_ok=True)
(root/'config/iris.properties').write_text('enableShaders=true\nshaderPack=Candid-Test\n')
