#version 430 core

in vec2 aTexCoords;
uniform sampler2D uTexture;
out vec4 FragColor;

void main() {
    // Edge threshold for determining the text edge
    float edgeThreshold = 0.9;
    // Width of the smoothing band
    float smoothWidth = 0.01;

    // Sample the distance field
    float distance = texture(uTexture, aTexCoords).r;

    // Apply threshold with smoothing
    float alpha = smoothstep(edgeThreshold - smoothWidth, edgeThreshold + smoothWidth, distance);

    // Output the final color
    FragColor = vec4(1.0, 1.0, 1.0, alpha);
}