package net.perfectdreams.gessyban

import net.perfectdreams.harmony.gl.textures.Texture

interface TextureManager {
    fun loadTexture(
        filePath: String,
        textureInterpolationType: Int
    ): Texture
}
