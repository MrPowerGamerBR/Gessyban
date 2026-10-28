package net.perfectdreams.gessyban

import net.perfectdreams.harmony.gl.textures.Texture

interface TextureManager {
    suspend fun loadTexture(
        filePath: String,
        textureInterpolationType: Int
    ): Texture
}
