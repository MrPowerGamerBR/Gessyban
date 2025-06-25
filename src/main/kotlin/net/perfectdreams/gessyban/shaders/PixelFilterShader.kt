package net.perfectdreams.gessyban.shaders

import net.perfectdreams.harmony.gl.shaders.GameShader

class PixelFilterShader(programId: Int) : GameShader(programId) {
    val uProjection = uniformMatrix4fv("uProjection")
    val uView = uniformMatrix4fv("uView")
    val uModel = uniformMatrix4fv("uModel")

    val uScreenTexture = uniform1Sampler2D("uScreenTexture")
    val uDepthTexture = uniform1Sampler2D("uDepthTexture")
    val uUniqueObjectIdTexture = uniform1Sampler2D("uUniqueObjectIdTexture")
    val uNormalsTexture = uniform1Sampler2D("uNormalsTexture")
}