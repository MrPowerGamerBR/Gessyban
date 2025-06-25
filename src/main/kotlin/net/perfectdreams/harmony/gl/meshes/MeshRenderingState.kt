package net.perfectdreams.harmony.gl.meshes

import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30.glBindVertexArray

// A VAO is like a "container" for all the data needed to render a mesh
// Which is why the name of this is a rendering state
class MeshRenderingState(val vaoId: Int, val vertexCount: Int) {
    fun render() {
        glBindVertexArray(vaoId)
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
    }
}