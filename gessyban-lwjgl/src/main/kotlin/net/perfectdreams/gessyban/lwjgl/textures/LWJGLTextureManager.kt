package net.perfectdreams.gessyban.lwjgl.textures

import net.perfectdreams.gessyban.TextureManager
import net.perfectdreams.gessyban.VirtualFileSystem
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.textures.Texture
import org.lwjgl.glfw.GLFW.glfwSetWindowIcon
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.system.MemoryUtil

class LWJGLTextureManager(val gpu: GPUResources, val vfs: VirtualFileSystem) : TextureManager {
    override fun loadTexture(
        filePath: String,
        textureInterpolationType: Int
    ): Texture {
        val data = vfs.files[filePath] ?: error("File $filePath does not exist!")
        val textureSource = STBImageDecoder.decode(data)

        return gpu.createTexture(
            textureSource,
            GLConstants.GL_CLAMP_TO_EDGE,
            GLConstants.GL_CLAMP_TO_EDGE,
            textureInterpolationType,
            textureInterpolationType
        )
    }

    fun setApplicationIcon(window: Long, filePath: String) {
        val data = vfs.files[filePath] ?: error("File $filePath does not exist!")
        val textureSource = STBImageDecoder.decode(data)

        val pixels = MemoryUtil.memAlloc(textureSource.pixels.size)
        pixels.put(textureSource.pixels)
        pixels.flip()

        val iconImages = GLFWImage.malloc(1)
        try {
            iconImages.position(0)
                .width(textureSource.width)
                .height(textureSource.height)
                .pixels(pixels)

            // Set the window icon
            glfwSetWindowIcon(window, iconImages)
        } finally {
            iconImages.free()
            MemoryUtil.memFree(pixels)
        }
    }
}
