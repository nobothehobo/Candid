#version 330 compatibility
uniform sampler2D colortex0;
in vec2 screenUv;
layout(location = 0) out vec4 finalColor;
void main() {
    // A deliberately strong test-only signature, asserted in the persisted PNG.
    finalColor = vec4(texture(colortex0, screenUv).rgb * vec3(1.0, 0.15, 0.15), 1.0);
}
