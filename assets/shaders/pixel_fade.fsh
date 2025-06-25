#version 430 core

in vec2 aTexCoords;
uniform sampler2D uScreenTexture;
uniform float uFadeAmount;

out vec4 FragColor;

void main() {
    if (uFadeAmount != 0.0) {
        float pixelSize = 1.0 + (128.0 * uFadeAmount);

        // Calculate the pixel size in texture coordinates
        vec2 texelSize = 1.0 / textureSize(uScreenTexture, 0);
        vec2 pixelizationFactor = pixelSize * texelSize;

        // Get the pixelated texture coordinate
        vec2 pixelatedCoord = floor(aTexCoords / pixelizationFactor) * pixelizationFactor;

        vec4 newColor = texture(uScreenTexture, pixelatedCoord);

        vec4 fadedColor = newColor - vec4(1.0 * uFadeAmount, 1.0 * uFadeAmount, 1.0 * uFadeAmount, 1.0 * uFadeAmount);

        FragColor = fadedColor;
    } else {
        FragColor = vec4(texture(uScreenTexture, aTexCoords));
    }
}