package net.perfectdreams.oldharmony.gl.textures

import net.perfectdreams.harmony.gl.ByteArrayTextureSource
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.textures.Texture
import org.lwjgl.glfw.GLFW.glfwSetWindowIcon
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.Spring
import kotlin.use

class TextureManager(val gpu: GPUResources) {
    fun loadTexture(
        filePath: String,
        textureInterpolationType: Int
    ): Texture {
        // stbi_set_flip_vertically_on_load(true)

        var imageBuffer: ByteBuffer?
        // Load image file into a ByteBuffer
        val path = Paths.get(filePath)
        if (Files.exists(path)) {
            Files.newByteChannel(path).use { fc ->
                imageBuffer = MemoryUtil.memAlloc(fc.size().toInt() + 1)
                while (fc.read(imageBuffer) != -1) {
                    // Loop until end of file
                }
            }
        } else {
            // Fallback for resources inside a JAR
            Texture::class.java.getResourceAsStream("/" + filePath).use { source ->
                Channels.newChannel(source).use { rbc ->
                    imageBuffer = MemoryUtil.memAlloc(8192) // 8KB initial buffer
                    while (true) {
                        val bytes = rbc.read(imageBuffer)
                        if (bytes == -1) {
                            break
                        }
                        if (imageBuffer!!.remaining() == 0) {
                            imageBuffer = MemoryUtil.memRealloc(
                                imageBuffer,
                                imageBuffer!!.capacity() * 2
                            )
                        }
                    }
                }
            }
        }
        imageBuffer!!.flip()

        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            // Decode the image
            val decodedImage = STBImage.stbi_load_from_memory(
                imageBuffer,
                w,
                h,
                channels,
                4 // Force 4 channels (RGBA)
            )
            if (decodedImage == null) {
                throw kotlin.RuntimeException(
                    "Failed to load image: " + STBImage.stbi_failure_reason()
                )
            }

            val width = w.get()
            val height = h.get()

            val realPixels = ByteBuffer
                .allocate(decodedImage.capacity())
                .put(decodedImage)
                .flip()
            decodedImage.flip()
            STBImage.stbi_image_free(decodedImage)
            return gpu.createTexture(ByteArrayTextureSource(width, height, realPixels.array()), GLConstants.GL_CLAMP_TO_EDGE, GLConstants.GL_CLAMP_TO_EDGE, textureInterpolationType, textureInterpolationType)
        }
    }

    fun setApplicationIcon(
        window: Long,
        filePath: String,
        flipVerticallyOnLoad: Boolean
    ) {
        stbi_set_flip_vertically_on_load(flipVerticallyOnLoad)

        var imageBuffer: ByteBuffer?

        // Load image file into a ByteBuffer
        val path = Paths.get(filePath)

        if (!Files.exists(path))
            error("Path does not exist! $path")

        Files.newByteChannel(path).use { fc ->
            imageBuffer = MemoryUtil.memAlloc(fc.size().toInt() + 1)
            while (fc.read(imageBuffer) != -1) {
                // Loop until end of file
            }
        }

        imageBuffer!!.flip()

        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            // Decode the image
            val decodedImage = STBImage.stbi_load_from_memory(
                imageBuffer,
                w,
                h,
                channels,
                4 // Force 4 channels (RGBA)
            )

            if (decodedImage == null)
                error("Failed to load image: " + STBImage.stbi_failure_reason())

            val width = w.get()
            val height = h.get()

            // Create a GLFWImage
            val iconImages = GLFWImage.malloc(1)
            iconImages.position(0)
                .width(w.get(0))
                .height(h.get(0))
                .pixels(decodedImage)

            // Set the window icon
            glfwSetWindowIcon(window, iconImages);

            // --- Cleanup ---
            STBImage.stbi_image_free(decodedImage) // Free the decoded image memory
            MemoryUtil.memFree(imageBuffer) // Free the raw file buffer
        }
    }
}