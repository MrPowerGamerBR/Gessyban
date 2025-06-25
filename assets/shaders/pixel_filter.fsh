#version 430 core

in vec2 aTexCoords;
uniform sampler2D uScreenTexture;
uniform sampler2D uDepthTexture;
uniform sampler2D uUniqueObjectIdTexture;
uniform sampler2D uNormalsTexture;
uniform float uTime;
uniform bool uApplyRedEffect;
uniform vec4 uColorOverride;

out vec4 FragColor;

void main() {
    float pixelSize = 0.003;
    vec4 textureColor = vec4(texture(uScreenTexture, aTexCoords));

    vec4 uniqueOIDColor = vec4(texture(uUniqueObjectIdTexture, aTexCoords));
    vec4 depthTextureColor = vec4(texture(uDepthTexture, aTexCoords));
    vec4 normalsColor = vec4(texture(uNormalsTexture, aTexCoords));

    vec4 textureColorLeft = vec4(texture(uScreenTexture, vec2(aTexCoords.x - pixelSize, aTexCoords.y)));
    vec4 textureColorRight = vec4(texture(uScreenTexture, vec2(aTexCoords.x + pixelSize, aTexCoords.y)));
    vec4 textureColorUp = vec4(texture(uScreenTexture, vec2(aTexCoords.x, aTexCoords.y - pixelSize)));
    vec4 textureColorDown = vec4(texture(uScreenTexture, vec2(aTexCoords.x, aTexCoords.y + pixelSize)));

    vec4 normalLeft = vec4(texture(uNormalsTexture, vec2(aTexCoords.x - pixelSize, aTexCoords.y)));
    vec4 normalRight = vec4(texture(uNormalsTexture, vec2(aTexCoords.x + pixelSize, aTexCoords.y)));
    vec4 normalUp = vec4(texture(uNormalsTexture, vec2(aTexCoords.x, aTexCoords.y - pixelSize)));
    vec4 normalDown = vec4(texture(uNormalsTexture, vec2(aTexCoords.x, aTexCoords.y + pixelSize)));

    vec4 depthTextureColorLeft = vec4(texture(uDepthTexture, vec2(aTexCoords.x - pixelSize, aTexCoords.y)));
    vec4 depthTextureColorRight = vec4(texture(uDepthTexture, vec2(aTexCoords.x + pixelSize, aTexCoords.y)));
    vec4 depthTextureColorUp = vec4(texture(uDepthTexture, vec2(aTexCoords.x, aTexCoords.y - pixelSize)));
    vec4 depthTextureColorDown = vec4(texture(uDepthTexture, vec2(aTexCoords.x, aTexCoords.y + pixelSize)));

    vec4 uniqueOIDTextureColorLeft = vec4(texture(uUniqueObjectIdTexture, vec2(aTexCoords.x - pixelSize, aTexCoords.y)));
    vec4 uniqueOIDTextureColorRight = vec4(texture(uUniqueObjectIdTexture, vec2(aTexCoords.x + pixelSize, aTexCoords.y)));
    vec4 uniqueOIDTextureColorUp = vec4(texture(uUniqueObjectIdTexture, vec2(aTexCoords.x, aTexCoords.y - pixelSize)));
    vec4 uniqueOIDTextureColorDown = vec4(texture(uUniqueObjectIdTexture, vec2(aTexCoords.x, aTexCoords.y + pixelSize)));

    // Check if it is a border
    bool isBorder = false;
    bool isInnerBorder = false;

    vec4 outputColor = textureColor;

    // Decode normals from [0, 1] range to [-1, 1]
    vec3 pNormals = normalsColor.rgb * 2.0 - 1.0;
    vec3 pNormalLeft = normalLeft.rgb * 2.0 - 1.0;
    vec3 pNormalRight = normalRight.rgb * 2.0 - 1.0;
    vec3 pNormalUp = normalUp.rgb * 2.0 - 1.0;
    vec3 pNormalDown = normalDown.rgb * 2.0 - 1.0;

    // Calculate the difference between the current normal and adjacent normals
    float diffLeft = dot(pNormals, pNormalLeft);
    float diffRight = dot(pNormals, pNormalRight);
    float diffUp = dot(pNormals, pNormalUp);
    float diffDown = dot(pNormals, pNormalDown);

    // Dot product close to 1.0 = angle is very smol
    // Dot product 0.0 = angle is 90 degrees
    // Dot product -1.0 = angle is 180 degrees
    // 0.01 seems to be the minimum that we can go
    float normalHighlightsThreshold = 0.01;

    // We have two independent highlight checks:
    // 1. One is with normals, this is useful for things like cubes
    // 2. Another is with the depth buffer, this is useful for more complex shapes like the gessy mesh

    // We need to know...
    // 1. Are we above the threshold?
    // 2. Are we highlighting ourselves?
    // 3. Is our depth higher (near the camera == darker) than the compared pixel? (to avoid things getting double highlighted)
    if (depthTextureColorLeft.r > depthTextureColor.r && normalHighlightsThreshold > diffLeft && uniqueOIDColor == uniqueOIDTextureColorLeft)
        isInnerBorder = true;
    if (depthTextureColorRight.r > depthTextureColor.r && normalHighlightsThreshold > diffRight && uniqueOIDColor == uniqueOIDTextureColorRight)
        isInnerBorder = true;
    if (depthTextureColorUp.r > depthTextureColor.r && normalHighlightsThreshold > diffUp && uniqueOIDColor == uniqueOIDTextureColorUp)
        isInnerBorder = true;
    if (depthTextureColorDown.r > depthTextureColor.r && normalHighlightsThreshold > diffDown && uniqueOIDColor == uniqueOIDTextureColorDown)
        isInnerBorder = true;

    // Another check...
    // Darker = near
    // We want to prioritize highlighting ONLY pixels that are NEAR us
    // TODO: Is this *really* needed?
    /* if (depthTextureColorLeft.r > depthTextureColor.r && distance(depthTextureColor, depthTextureColorLeft) >= 0.005 && uniqueOIDColor == uniqueOIDTextureColorLeft)
        isInnerBorder = true;
    if (depthTextureColorRight.r > depthTextureColor.r && distance(depthTextureColor, depthTextureColorRight) >= 0.005 && uniqueOIDColor == uniqueOIDTextureColorRight)
        isInnerBorder = true;
    if (depthTextureColorUp.r > depthTextureColor.r && distance(depthTextureColor, depthTextureColorUp) >= 0.005 && uniqueOIDColor == uniqueOIDTextureColorUp)
        isInnerBorder = true;
    if (depthTextureColorDown.r > depthTextureColor.r && distance(depthTextureColor, depthTextureColorDown) >= 0.005 && uniqueOIDColor == uniqueOIDTextureColorDown)
        isInnerBorder = true; */

    // We only want alpha == 1.0 to avoid getting the background
    if (uniqueOIDColor.a == 1.0) {
        if (uniqueOIDColor != uniqueOIDTextureColorLeft)
            isBorder = true;

        if (uniqueOIDColor != uniqueOIDTextureColorRight)
            isBorder = true;

        if (uniqueOIDColor != uniqueOIDTextureColorUp)
            isBorder = true;

        if (uniqueOIDColor != uniqueOIDTextureColorDown)
            isBorder = true;
    }

    if (isInnerBorder) {
        outputColor *= vec4(1.25, 1.25, 1.25, 1.0);
        // outputColor = vec4(0.0, 1.0, 1.0, 1.0);
        // outputColor = vec4(0.0, 1.0, 1.0, 1.0);
    }

    if (isBorder) {
        outputColor = vec4(0.0, 0.0, 0.0, 1.0);
    }

    if (uApplyRedEffect) {
        outputColor.r += sin(uTime);
        outputColor.g -= sin(uTime);
        outputColor.b *= sin(uTime);
    }

    if (uColorOverride.a != 0.0) {
        outputColor *= uColorOverride;
    }

    // vec4 color = vec4(0.0, 1.0, 1.0, 1.0);

    // if (int(gl_FragCoord.y) % 4 == 0) {
    //     color -= vec4(0.05, 0.05, 0.05, 0.0);
    // }

    // FragColor = color; // vec4(texture(screenTexture, TexCoords));
    FragColor = outputColor;
    // FragColor = vec4(0.0, 0.0, 1.0, 1.0);
}