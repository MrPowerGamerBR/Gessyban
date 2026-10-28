package net.perfectdreams.oldharmony.gl.meshes

import net.perfectdreams.harmony.gl.GLConstants.GL_TRIANGLES
import net.perfectdreams.harmony.gl.GLContext

// A VAO is like a "container" for all the data needed to render a mesh
// Which is why the name of this is a rendering state
class MeshRenderingState(val gl: GLContext, val vaoId: Int, val vertexCount: Int) {
    fun render() {
        gl.bindVertexArray(vaoId)
        gl.drawArrays(GL_TRIANGLES, 0, vertexCount)
    }
}