package net.perfectdreams.gessyban.webgl2.textures

import js.array.toJsArray
import js.buffer.toArrayBuffer
import js.objects.unsafeJso
import net.perfectdreams.gessyban.TextureManager
import net.perfectdreams.gessyban.VirtualFileSystem
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.webgl2.BrowserImageBitmapTextureSource
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.textures.Texture
import web.blob.Blob
import web.images.createImageBitmap

/**
 * A [TextureManager] that loads textures from images that were already decoded by the browser
 */
class WebTextureManager(val device: RenderDevice, val vfs: VirtualFileSystem) : TextureManager {
    override suspend fun loadTexture(
        filePath: String,
        textureInterpolationType: Int
    ): Texture {
        val data = vfs.files[filePath]!!

        val blob = Blob(
            listOf(data.toArrayBuffer()).toJsArray(),
            unsafeJso {
                type = "application/octet-stream"
            }
        )

        val image = createImageBitmap(blob)

        return device.createTexture(
            BrowserImageBitmapTextureSource(image),
            GLConstants.GL_CLAMP_TO_EDGE,
            GLConstants.GL_CLAMP_TO_EDGE,
            textureInterpolationType,
            textureInterpolationType
        )
    }
}
