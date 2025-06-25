package net.perfectdreams.lorituber.framebuffers

import org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_LINEAR
import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.opengl.GL11.GL_RGB
import org.lwjgl.opengl.GL11.GL_RGBA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S
import org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL11.glGenTextures
import org.lwjgl.opengl.GL11.glTexImage2D
import org.lwjgl.opengl.GL11.glTexParameteri
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0
import org.lwjgl.opengl.GL30.GL_DEPTH24_STENCIL8
import org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT
import org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL_ATTACHMENT
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE
import org.lwjgl.opengl.GL30.GL_RENDERBUFFER
import org.lwjgl.opengl.GL30.glBindFramebuffer
import org.lwjgl.opengl.GL30.glBindRenderbuffer
import org.lwjgl.opengl.GL30.glCheckFramebufferStatus
import org.lwjgl.opengl.GL30.glFramebufferRenderbuffer
import org.lwjgl.opengl.GL30.glFramebufferTexture2D
import org.lwjgl.opengl.GL30.glGenFramebuffers
import org.lwjgl.opengl.GL30.glGenRenderbuffers
import org.lwjgl.opengl.GL30.glRenderbufferStorage
import java.nio.ByteBuffer

class FramebufferManager {
    fun createFramebuffer(width: Int, height: Int): Framebuffer {
        // 1. Create the Framebuffer Object (FBO)
        val fboId = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, fboId)

        // 2. Create the Color Attachment Texture
        val textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)
        // Allocate memory for the texture, but don't upload any data (pass null)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            null as ByteBuffer?
        )
        // Set texture filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        // Attach the texture to the FBO
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0, // We're attaching to the first color output
            GL_TEXTURE_2D,
            textureId,
            0
        )

        // 3. Create the Renderbuffer Object (RBO) for Depth and Stencil
        val rboId = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, rboId)

        // 3. Create and attach Depth Texture (THE PART YOU NEED!)
        val depthTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTextureId)
        // Setup depth texture storage:
        // GL_DEPTH_COMPONENT: Internal format for depth
        // GL_DEPTH_COMPONENT: Format of the pixel data
        // GL_FLOAT: Type of the pixel data (float is common for depth)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        // Set texture parameters (important for depth textures)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST) // Or GL_NEAREST for pixel-perfect sampling
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST) // Or GL_NEAREST
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        // Attach depth texture to FBO
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTextureId, 0)
        glBindTexture(GL_TEXTURE_2D, 0) // Unbind texture

        // 4. Check if the framebuffer is complete
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer is not complete!")
        }

        // 5. Unbind the FBO to avoid accidentally rendering to it
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        return Framebuffer(
            fboId,
            textureId,
            depthTextureId,
            width,
            height
        )
    }
}