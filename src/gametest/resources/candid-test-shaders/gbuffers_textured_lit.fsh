#version 330 compatibility
uniform sampler2D gtexture;
uniform sampler2D lightmap;
in vec2 atlasUv;
in vec2 lightUv;
in vec4 vertexTint;
/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 sceneColor;
void main() {
    vec4 surface = texture(gtexture, atlasUv) * vertexTint;
    if (surface.a < 0.1) discard;
    sceneColor = vec4(surface.rgb * texture(lightmap, lightUv).rgb, surface.a);
}
