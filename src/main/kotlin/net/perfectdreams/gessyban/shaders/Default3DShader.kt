package net.perfectdreams.gessyban.shaders

import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.shaders.ShaderProgram

class Default3DShader(gl: GLContext, programId: Int) : ShaderProgram(gl, programId) {
    val uProjection = uniformMatrix4fv("uProjection")
    val uView = uniformMatrix4fv("uView")
    val uModel = uniformMatrix4fv("uModel")
    val uTexture = uniform1Sampler2D("uTexture")
    val uBoneMatrices = uniformMatrix4fv("uBoneMatrices")
    val uIsObjectIdPass = uniform1Bool("uIsObjectIdPass")
    val uObjectIdRGB = uniform3f("uObjectIdRGB")
    val uUseNormalsAsColor = uniform1Bool("uUseNormalsAsColor")
}