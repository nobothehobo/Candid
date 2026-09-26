#version 330 compatibility
out vec2 atlasUv;
out vec2 lightUv;
out vec4 vertexTint;
void main() {
    gl_Position = ftransform();
    atlasUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lightUv = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    vertexTint = gl_Color;
}
