package net.perfectdreams.gessyban.shaders

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.shaders.ShaderProgram

class PixelFadeShader(gl: GLContext, programId: Int) : ShaderProgram(gl, programId) {
    val uProjection = uniformMatrix4fv("uProjection")
    val uView = uniformMatrix4fv("uView")
    val uModel = uniformMatrix4fv("uModel")

    val uScreenTexture = uniform1Sampler2D("uScreenTexture")
    val uFadeAmount = uniform1f("uFadeAmount")
}