#version 330 compatibility
out vec4 vertexTint;
void main() {
    gl_Position = ftransform();
    vertexTint = gl_Color;
}
