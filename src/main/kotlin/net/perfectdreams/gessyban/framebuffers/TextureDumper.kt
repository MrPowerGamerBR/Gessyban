package net.perfectdreams.gessyban.framebuffers

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.ARBInternalformatQuery2.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_RGBA
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glGetTexImage
import org.lwjgl.opengl.GL11.glReadPixels
import org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.opengl.GL30.glCheckFramebufferStatus
import org.lwjgl.opengl.GL30.glDeleteFramebuffers
import org.lwjgl.opengl.GL30.glFramebufferTexture2D
import org.lwjgl.opengl.GL30.glGenFramebuffers
import org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write
import org.lwjgl.stb.STBImageWrite.stbi_write_png
import org.lwjgl.system.MemoryUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.file.Path

object TextureDumper {
    /**
     * Dumps the content of an OpenGL texture to a PNG file.
     *
     * @param textureId The ID of the texture to dump.
     * @param width The width of the texture.
     * @param height The height of the texture.
     * @param filePath The path where the PNG file will be saved (e.g., "C:/path/to/image.png").
     */
    fun dumpTexture(textureId: Int, width: Int, height: Int, filePath: String) {
        // Bind the texture so we can work with it
        glBindTexture(GL_TEXTURE_2D, textureId)

        // Create a byte buffer to hold the pixel data.
        // We use 4 channels for RGBA.
        val buffer: ByteBuffer = BufferUtils.createByteBuffer(width * height * 4)

        // Read the pixel data from the currently bound texture
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

        // STB expects the image data to be flipped vertically,
        // which is the opposite of how OpenGL stores it.
        stbi_flip_vertically_on_write(true)

        // Write the image data to a PNG file.
        // The last argument is the stride, which is width * number of channels.
        val success = stbi_write_png(filePath, width, height, 4, buffer, width * 4)

        if (!success) {
            System.err.println("Failed to save texture to $filePath")
        } else {
            println("Texture saved to $filePath")
        }

        // Unbind the texture
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun dumpDepthTexture(textureId: Int, width: Int, height: Int, filePath: String) {
        glBindTexture(GL_TEXTURE_2D, textureId)

        // Create a float buffer to hold the depth data
        val floatBuffer: FloatBuffer = BufferUtils.createFloatBuffer(width * height)

        // Read the depth component data from the texture into the float buffer
        glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_FLOAT, floatBuffer)

        // Create a byte buffer to hold the 8-bit grayscale image data
        val byteBuffer: ByteBuffer = BufferUtils.createByteBuffer(width * height)

        // Convert the float depth data (0.0 to 1.0) to 8-bit grayscale values (0 to 255)
        // and put them into the byte buffer.
        while (floatBuffer.hasRemaining()) {
            val depthValue = floatBuffer.get()
            val byteValue = (depthValue * 255.0f).toInt().toByte()
            byteBuffer.put(byteValue)
        }
        byteBuffer.flip() // Prepare the buffer for reading by stbi_write_png

        // STB expects the image data to be flipped vertically
        stbi_flip_vertically_on_write(false)

        // Write the grayscale data to a PNG file.
        // The '1' indicates that we have 1 channel (grayscale).
        // The stride is the width in bytes, which is just the width for a 1-channel image.
        val success = stbi_write_png(filePath, width, height, 1, byteBuffer, width)

        if (!success) {
            System.err.println("Failed to save depth texture to $filePath")
        } else {
            println("Depth texture saved to $filePath")
        }

        // Unbind the texture
        glBindTexture(GL_TEXTURE_2D, 0)
    }
}