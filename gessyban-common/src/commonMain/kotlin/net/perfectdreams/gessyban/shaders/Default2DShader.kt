package net.perfectdreams.gessyban.shaders

import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.render.batcher.SpriteShader

class Default2DShader(gl: GLContext, programId: GLProgram) : SpriteShader(gl, programId) {
    val uProjection = uniformMatrix4fv("uProjection")
    override val uTexture = uniformSampler2D("uTexture")
}