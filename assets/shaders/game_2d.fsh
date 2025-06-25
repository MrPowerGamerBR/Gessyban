#version 430 core

in vec2 aTexCoords;
uniform sampler2D uTexture;
out vec4 FragColor;

void main() {
    FragColor = vec4(texture(uTexture, aTexCoords));
}