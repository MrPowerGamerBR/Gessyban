package net.perfectdreams.gessyban

import net.perfectdreams.harmony.render.textures.Texture

interface TextureManager {
    suspend fun loadTexture(
        filePath: String,
        textureInterpolationType: Int
    ): Texture
}
