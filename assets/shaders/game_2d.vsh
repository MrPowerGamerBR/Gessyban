#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aUV;
layout (location = 2) in vec3 aBlendColor;

uniform mat4 uProjection;

out vec2 vUV;

void main() {
    vUV = aUV;
    gl_Position = uProjection * vec4(aPos, 1.0, 1.0);
}