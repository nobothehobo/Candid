#version 330 compatibility
in vec4 vertexTint;
/* RENDERTARGETS: 0 */
layout(location = 0) out vec4 sceneColor;
void main() {
    sceneColor = vertexTint;
}
