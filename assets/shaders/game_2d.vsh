#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aUvPos;

uniform mat4 uProjection;
uniform mat4 uModel;
uniform mat4 uView;

out vec2 aTexCoords;

void main() {
    aTexCoords = aUvPos;
    gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0, 1.0);
}