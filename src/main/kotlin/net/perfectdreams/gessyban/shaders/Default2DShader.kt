package net.perfectdreams.gessyban.shaders

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.graphics.batcher.SpriteShader

class Default2DShader(gl: GLContext, programId: Int) : SpriteShader(gl, programId) {
    val uProjection = uniformMatrix4fv("uProjection")
    override val uTexture = uniform1Sampler2D("uTexture")
}