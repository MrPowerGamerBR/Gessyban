#version 430 core

in vec3 vNormal;
in vec2 vUVCoords;

flat in int vVertexBoneId;
uniform sampler2D uTexture;
uniform bool uIsObjectIdPass;
uniform vec3 uObjectIdRGB;
uniform bool uUseNormalsAsColor;
out vec4 FragColor;

void main() {
    FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    if (false && vVertexBoneId == 2) {
        FragColor = vec4(0.0, 0.0, 1.0, 1.0);
        return;
    }

    if (uIsObjectIdPass) {
        FragColor = vec4(uObjectIdRGB, 1.0);
        return;
    }

    if (uUseNormalsAsColor) {
        FragColor = vec4((vNormal.r + 1.0) / 2, (vNormal.g + 1.0) / 2, (vNormal.b + 1.0) / 2, 1.0);
        return;
    }

    FragColor = vec4(texture(uTexture, vUVCoords));
    // FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}