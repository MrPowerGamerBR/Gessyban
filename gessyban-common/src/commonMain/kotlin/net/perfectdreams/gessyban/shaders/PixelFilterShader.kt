package net.perfectdreams.gessyban.shaders

import net.perfectdreams.harmony.gl.GLProgram
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.render.shaders.ShaderProgram

class PixelFilterShader(gl: GLContext, programId: GLProgram) : ShaderProgram(gl, programId) {
    val uProjection = uniformMatrix4fv("uProjection")
    val uView = uniformMatrix4fv("uView")
    val uModel = uniformMatrix4fv("uModel")

    val uScreenTexture = uniformSampler2D("uScreenTexture")
    val uDepthTexture = uniformSampler2D("uDepthTexture")
    val uUniqueObjectIdTexture = uniformSampler2D("uUniqueObjectIdTexture")
    val uNormalsTexture = uniformSampler2D("uNormalsTexture")
}