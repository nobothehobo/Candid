#version 330 compatibility
uniform sampler2D gtexture;
in vec2 atlasUv;
in vec4 vertexTint;
/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 sceneColor;
void main() {
    sceneColor = texture(gtexture, atlasUv) * vertexTint;
    if (sceneColor.a < 0.1) discard;
}
