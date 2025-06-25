#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aUVCoords;
layout (location = 2) in int aVertexBoneId;
layout (location = 3) in vec3 aNormal;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uBoneMatrices[19];

out vec2 vUVCoords;
out vec3 vNormal;

flat out int vVertexBoneId;

void main() {
    // vNormal = aNormal;
    vUVCoords = aUVCoords;
    vVertexBoneId = aVertexBoneId;
    vNormal = aNormal;

    // Get bone transformation for this vertex
    mat4 boneTransform = uBoneMatrices[aVertexBoneId];

    // Initialize transformed position
    vec4 transformedPosition = boneTransform * vec4(aPos, 1.0);

    mat4 mvp = uProjection * uView * uModel;

    if (false) {
        gl_Position = mvp * vec4(aPos, 1.0);
        return;
    }

    gl_Position = mvp * transformedPosition;
}