package net.perfectdreams.lorituber.framebuffers

import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.glBindFramebuffer

class Framebuffer(
    val framebufferId: Int,
    val textureId: Int,
    val depthTextureId: Int,
    val width: Int,
    val height: Int
) {
    fun bind(block: () -> (Unit)) {
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebufferId)
        glViewport(0, 0, this.width, this.height)

        block.invoke()
    }
}