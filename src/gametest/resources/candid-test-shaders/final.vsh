#version 330 compatibility
out vec2 screenUv;
void main() {
    gl_Position = ftransform();
    screenUv = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
}
