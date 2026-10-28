package net.perfectdreams.gessyban.meshes

import com.mrpowergamerbr.starfait.Matrix4f
import kotlinx.serialization.json.Json
import net.perfectdreams.gessyban.FontCharacter
import net.perfectdreams.gessyban.skeletons.NewBone
import net.perfectdreams.gessyban.skeletons.Skeleton
import net.perfectdreams.harmony.gl.GLConstants.GL_ARRAY_BUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_FLOAT
import net.perfectdreams.harmony.gl.GLConstants.GL_INT
import net.perfectdreams.harmony.gl.GLConstants.GL_STATIC_DRAW
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.vertex.buildVertexAttributes
import net.perfectdreams.oldharmony.gl.meshes.MeshRenderingState

class JsonMeshManager {
    // This variation loads a mesh, but won't assign any bones for any of the vertices
    // Use a skeleton for animation!
    fun createJsonMeshVAO(gl: GLContext, meshModel: JsonMesh): MeshRenderingState {
        // Initial validation
        for (face in meshModel.faces) {
            meshModel.vertices.getOrNull(face.v0) ?: error("Missing vertex!")
            meshModel.vertices.getOrNull(face.v1) ?: error("Missing vertex!")
            meshModel.vertices.getOrNull(face.v2) ?: error("Missing vertex!")
        }

        if (false) {
            for (vertex in meshModel.vertices) {
                if (vertex.vertexGroups.size >= 2)
                    error("Too many vertex groups for vertex $vertex!")
            }
        }

        println("Faces: ${meshModel.faces.size}")
        println("Vertices: ${meshModel.vertices.size}")

        // We can't use EBOs here because the same "vertex" may have different normals
        // All values are initialized to zero

        // Each face has 3 vertices, each vertex has 3 coordinates (x, y, z)
        val vertices = FloatArray(meshModel.faces.size * 9)

        // Each face has 3 vertices, each vertex has 2 UV coordinates
        val textureCoordinates = FloatArray(meshModel.faces.size * 6)

        // Each vertex has 1 bone index ID
        val vertexBoneIds = IntArray(meshModel.faces.size * 3)

        // Each face has 3 vertices, each vertex has 3 normals
        val normals = FloatArray(meshModel.faces.size * 9)

        var arrayVertexIdx = 0
        var arrayUVIdx = 0
        var arrayVertexBonesIdx = 0
        var arrayNormalsIdx = 0

        for (face in meshModel.faces) {
            val vertex0 = meshModel.vertices[face.v0]
            val vertex1 = meshModel.vertices[face.v1]
            val vertex2 = meshModel.vertices[face.v2]

            vertices[arrayVertexIdx++] = vertex0.x
            vertices[arrayVertexIdx++] = vertex0.y
            vertices[arrayVertexIdx++] = vertex0.z

            vertices[arrayVertexIdx++] = vertex1.x
            vertices[arrayVertexIdx++] = vertex1.y
            vertices[arrayVertexIdx++] = vertex1.z

            vertices[arrayVertexIdx++] = vertex2.x
            vertices[arrayVertexIdx++] = vertex2.y
            vertices[arrayVertexIdx++] = vertex2.z

            normals[arrayNormalsIdx++] = face.normal.x
            normals[arrayNormalsIdx++] = face.normal.y
            normals[arrayNormalsIdx++] = face.normal.z

            normals[arrayNormalsIdx++] = face.normal.x
            normals[arrayNormalsIdx++] = face.normal.y
            normals[arrayNormalsIdx++] = face.normal.z

            normals[arrayNormalsIdx++] = face.normal.x
            normals[arrayNormalsIdx++] = face.normal.y
            normals[arrayNormalsIdx++] = face.normal.z

            val textureCoordinates0 = face.uv0
            val textureCoordinates1 = face.uv1
            val textureCoordinates2 = face.uv2

            textureCoordinates[arrayUVIdx++] = textureCoordinates0.x
            textureCoordinates[arrayUVIdx++] = 1f - textureCoordinates0.y

            textureCoordinates[arrayUVIdx++] = textureCoordinates1.x
            textureCoordinates[arrayUVIdx++] = 1f - textureCoordinates1.y

            textureCoordinates[arrayUVIdx++] = textureCoordinates2.x
            textureCoordinates[arrayUVIdx++] = 1f - textureCoordinates2.y

            vertexBoneIds[arrayVertexBonesIdx++] = 0
            vertexBoneIds[arrayVertexBonesIdx++] = 0
            vertexBoneIds[arrayVertexBonesIdx++] = 0
        }

        for (v in vertexBoneIds) {
            println(v)
        }

        // Create and bind VAO
        val quadVAO = gl.genVertexArrays()
        gl.bindVertexArray(quadVAO)

        // Generate four VBOs
        // val vbosArray = IntArray(4)
        val vbo1 = gl.genBuffers()
        val vbo2 = gl.genBuffers()
        val vbo3 = gl.genBuffers()
        val vbo4 = gl.genBuffers()

        // Position VBO
        gl.enableVertexAttribArray(0)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo1)
        gl.bufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        gl.vertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        // Texture Coordinates VBO
        gl.enableVertexAttribArray(1)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo2)
        gl.bufferData(GL_ARRAY_BUFFER, textureCoordinates, GL_STATIC_DRAW)
        gl.vertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)

        // Vertex Bones VBO
        gl.enableVertexAttribArray(2)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo3)
        gl.bufferData(GL_ARRAY_BUFFER, vertexBoneIds, GL_STATIC_DRAW)
        // YES WE NEED TO USE glVertexAttribIPointer FOR GL_INT!!!
        gl.vertexAttribIPointer(2, 1, GL_INT, 0, 0)

        // Normals VBO
        gl.enableVertexAttribArray(3)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo4)
        gl.bufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW)
        gl.vertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0)

        // Unbind
        gl.bindBuffer(GL_ARRAY_BUFFER, 0)
        gl.bindVertexArray(0)
        // TODO:
        // glDisableVertexAttribArray(3)
        // glDisableVertexAttribArray(2)
        // glDisableVertexAttribArray(1)
        // glDisableVertexAttribArray(0)

        return MeshRenderingState(gl, quadVAO, meshModel.faces.size * 9)
    }

    // This creates a mesh for the 2D shader
    fun create2DQuad(gl: GLContext): MeshRenderingState {
        // We don't use JSON meshes for this because a 2D quad is so simple that we can hardcode it in here
        // Also because the 2D shader works differently from the 3D shader, so exporting a plane to be used in the 2D shader isn't straightforward

        // positions: vec2, uv: vec2
        val quadVertices = floatArrayOf(
            // The descriptions are as a full quad
            // If we do a triangle from 0f to 1f, everything will be positioned around the quad's top left corner

            // First triangle
            0.0f, 0.0f, 0.0f, 1.0f, // top left
            1.0f, 0.0f, 1.0f, 1.0f, // top right
            1.0f, 1.0f, 1.0f, 0.0f, // bottom right

            // Second triangle
            1.0f, 1.0f, 1.0f, 0.0f, // bottom right
            0.0f, 1.0f, 0.0f, 0.0f, // bottom left
            0.0f, 0.0f, 0.0f, 1.0f, // top left
        )

        val screenQuadVao = gl.genVertexArrays()
        val vbo = gl.genBuffers()

        gl.bindVertexArray(screenQuadVao)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo)
        gl.bufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW)

        // Index: The index of the vertex attribute
        // Size: The size of each entry
        // Type: The type
        // Normalized: Whether the data should be normalized
        // Stride: The byte offset between consecutive attributes
        // Pointer: Where the data is in the array

        // So, as an example...
        // For something with size 2 (two floats) in a array that has vec2 (pos), vec2 (uv)
        // If we want to access the second vec2, we would need to...
        // size = 2 (because each entry is 2 floats)
        // stride = 4 * Float.SIZE_BYTES (because each entry is [entry], *8 bytes for the pos*, [entry] ...), the stride is relative to the START of the vertex!
        // pointer = the data starts at 2 * Float.SIZE_BYTES

        val attributes = buildVertexAttributes {
            vec2(0) // Position attribute
            vec2(1) // UV attribute
        }
        attributes.apply(gl)

        return MeshRenderingState(gl, screenQuadVao, 6)
    }

    // This creates a mesh for the 2D shader
    fun createMesh2D(gl: GLContext, meshModel: JsonMesh): MeshRenderingState {
        val quadData = FloatArray(
            // Each face has 3 vertices, each vertex has 3 coordinates (x, y, z)
            // Each face has 3 vertices, each vertex has 2 UV coordinates
            (meshModel.faces.size * 9) + (meshModel.faces.size * 6)
        )

        var arrayIdx = 0

        for (face in meshModel.faces) {
            val vertex0 = meshModel.vertices[face.v0]
            val vertex1 = meshModel.vertices[face.v1]
            val vertex2 = meshModel.vertices[face.v2]

            val textureCoordinates0 = face.uv0
            val textureCoordinates1 = face.uv1
            val textureCoordinates2 = face.uv2

            quadData[arrayIdx++] = vertex0.x
            quadData[arrayIdx++] = vertex0.y

            quadData[arrayIdx++] = textureCoordinates0.x
            quadData[arrayIdx++] = textureCoordinates0.y

            quadData[arrayIdx++] = vertex1.x
            quadData[arrayIdx++] = vertex1.y

            quadData[arrayIdx++] = textureCoordinates1.x
            quadData[arrayIdx++] = textureCoordinates1.y

            quadData[arrayIdx++] = vertex2.x
            quadData[arrayIdx++] = vertex2.y

            quadData[arrayIdx++] = textureCoordinates2.x
            quadData[arrayIdx++] = textureCoordinates2.y
        }

        // Create and bind VAO
        val quadVAO = gl.genVertexArrays()
        gl.bindVertexArray(quadVAO)

        // Generate one VBO
        val vbosArray = gl.genBuffers()

        // Position VBO
        gl.enableVertexAttribArray(0)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbosArray)
        gl.bufferData(GL_ARRAY_BUFFER, quadData, GL_STATIC_DRAW)
        gl.vertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)

        // Texture Coordinates VBO
        gl.enableVertexAttribArray(1)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbosArray)
        gl.bufferData(GL_ARRAY_BUFFER, quadData, GL_STATIC_DRAW)
        gl.vertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.SIZE_BYTES, 2L * Float.SIZE_BYTES)

        // Unbind
        gl.bindBuffer(GL_ARRAY_BUFFER, 0)
        gl.bindVertexArray(0)
        // TODO:
        // glDisableVertexAttribArray(1)
        // glDisableVertexAttribArray(0)

        return MeshRenderingState(gl, quadVAO, meshModel.faces.size * 3)
    }

    fun createJsonMeshSkeleton(
        gl: GLContext,
        meshModel: JsonMesh,
        content: String
    ): Skeleton {
        val skeleton = Json.decodeFromString<Map<String, NewBone>>(content)

        // Initial validation
        for (face in meshModel.faces) {
            meshModel.vertices.getOrNull(face.v0) ?: error("Missing vertex!")
            meshModel.vertices.getOrNull(face.v1) ?: error("Missing vertex!")
            meshModel.vertices.getOrNull(face.v2) ?: error("Missing vertex!")
        }

        // Reenable this later!
        if (false) {
            for (vertex in meshModel.vertices) {
                if (vertex.vertexGroups.size >= 2)
                    error("Too many vertex groups for vertex $vertex!")
            }
        }

        // Reconciliation!
        // Before we start, we need to map the correct GLSL Group ID to each vertex group
        // Because an skeleton may have different IDs than the mesh
        val skeletonGroupNamesToSkeletonGroupIds = mutableMapOf<String, Int>()

        run {
            var idx = 0
            for (bone in skeleton) {
                skeletonGroupNamesToSkeletonGroupIds[bone.key] = idx++
            }
        }

        println("Faces: ${meshModel.faces.size}")
        println("Vertices: ${meshModel.vertices.size}")
        for ((groupName, groupId) in skeletonGroupNamesToSkeletonGroupIds) {
            println("Group $groupName -> $groupId")
        }

        // We can't use EBOs here because the same "vertex" may have different normals
        // All values are initialized to zero

        // Each face has 3 vertices, each vertex has 3 coordinates (x, y, z)
        val vertices = FloatArray(meshModel.faces.size * 9)

        // Each face has 3 vertices, each vertex has 2 UV coordinates
        val textureCoordinates = FloatArray(meshModel.faces.size * 6)

        // Each vertex has 1 bone index ID
        val vertexBoneIds = IntArray(meshModel.faces.size * 3)

        // Each face has 3 vertices, each vertex has 3 normals
        val normals = FloatArray(meshModel.faces.size * 9)

        var arrayVertexIdx = 0
        var arrayUVIdx = 0
        var arrayVertexBonesIdx = 0
        var arrayNormalsIdx = 0

        for (face in meshModel.faces) {
            val vertex0 = meshModel.vertices[face.v0]
            val vertex1 = meshModel.vertices[face.v1]
            val vertex2 = meshModel.vertices[face.v2]

            vertices[arrayVertexIdx++] = vertex0.x
            vertices[arrayVertexIdx++] = vertex0.y
            vertices[arrayVertexIdx++] = vertex0.z

            vertices[arrayVertexIdx++] = vertex1.x
            vertices[arrayVertexIdx++] = vertex1.y
            vertices[arrayVertexIdx++] = vertex1.z

            vertices[arrayVertexIdx++] = vertex2.x
            vertices[arrayVertexIdx++] = vertex2.y
            vertices[arrayVertexIdx++] = vertex2.z

            normals[arrayNormalsIdx++] = face.normal.x
            normals[arrayNormalsIdx++] = face.normal.y
            normals[arrayNormalsIdx++] = face.normal.z

            normals[arrayNormalsIdx++] = face.normal.x
            normals[arrayNormalsIdx++] = face.normal.y
            normals[arrayNormalsIdx++] = face.normal.z

            normals[arrayNormalsIdx++] = face.normal.x
            normals[arrayNormalsIdx++] = face.normal.y
            normals[arrayNormalsIdx++] = face.normal.z

            val textureCoordinates0 = face.uv0
            val textureCoordinates1 = face.uv1
            val textureCoordinates2 = face.uv2

            textureCoordinates[arrayUVIdx++] = textureCoordinates0.x
            textureCoordinates[arrayUVIdx++] = 1f - textureCoordinates0.y

            textureCoordinates[arrayUVIdx++] = textureCoordinates1.x
            textureCoordinates[arrayUVIdx++] = 1f - textureCoordinates1.y

            textureCoordinates[arrayUVIdx++] = textureCoordinates2.x
            textureCoordinates[arrayUVIdx++] = 1f - textureCoordinates2.y

            vertexBoneIds[arrayVertexBonesIdx++] = skeletonGroupNamesToSkeletonGroupIds[vertex0.vertexGroups.first()] ?: error("Could not find group ${vertex0.vertexGroups.first()}")
            vertexBoneIds[arrayVertexBonesIdx++] = skeletonGroupNamesToSkeletonGroupIds[vertex1.vertexGroups.first()] ?: error("Could not find group ${vertex0.vertexGroups.first()}")
            vertexBoneIds[arrayVertexBonesIdx++] = skeletonGroupNamesToSkeletonGroupIds[vertex2.vertexGroups.first()] ?: error("Could not find group ${vertex0.vertexGroups.first()}")
        }

        for (v in vertexBoneIds) {
            println(v)
        }

        // Create and bind VAO
        val quadVAO = gl.genVertexArrays()
        gl.bindVertexArray(quadVAO)

        // Generate four VBOs
        val vbo1 = gl.genBuffers()
        val vbo2 = gl.genBuffers()
        val vbo3 = gl.genBuffers()
        val vbo4 = gl.genBuffers()

        // Position VBO
        gl.enableVertexAttribArray(0)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo1)
        gl.bufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
        gl.vertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        // Texture Coordinates VBO
        gl.enableVertexAttribArray(1)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo2)
        gl.bufferData(GL_ARRAY_BUFFER, textureCoordinates, GL_STATIC_DRAW)
        gl.vertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)

        // Vertex Bones VBO
        gl.enableVertexAttribArray(2)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo3)
        gl.bufferData(GL_ARRAY_BUFFER, vertexBoneIds, GL_STATIC_DRAW)
        // YES WE NEED TO USE glVertexAttribIPointer FOR GL_INT!!!
        gl.vertexAttribIPointer(2, 1, GL_INT, 0, 0)

        // Normals VBO
        gl.enableVertexAttribArray(3)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo4)
        gl.bufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW)
        gl.vertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0)

        // Unbind
        gl.bindBuffer(GL_ARRAY_BUFFER, 0)
        gl.bindVertexArray(0)
        // TODO:
        // glDisableVertexAttribArray(2)
        // glDisableVertexAttribArray(1)
        // glDisableVertexAttribArray(0)

        // Skeleton tidbits
        val globalBindTransformsMatrices = mutableMapOf<String, Matrix4f>()

        for ((name, bone) in skeleton) {
            val parentMatrix = if (bone.parent == null) {
                Matrix4f()
            } else {
                globalBindTransformsMatrices[bone.parent] ?: error("Parent matrix not found for bone $name")
            }

            val thisMatrix = Matrix4f()
                .apply {
                    this.m00 = bone.localBindTransformMatrix[0]
                    this.m01 = bone.localBindTransformMatrix[1]
                    this.m02 = bone.localBindTransformMatrix[2]
                    this.m03 = bone.localBindTransformMatrix[3]

                    this.m10 = bone.localBindTransformMatrix[4]
                    this.m11 = bone.localBindTransformMatrix[5]
                    this.m12 = bone.localBindTransformMatrix[6]
                    this.m13 = bone.localBindTransformMatrix[7]

                    this.m20 = bone.localBindTransformMatrix[8]
                    this.m21 = bone.localBindTransformMatrix[9]
                    this.m22 = bone.localBindTransformMatrix[10]
                    this.m23 = bone.localBindTransformMatrix[11]

                    this.m30 = bone.localBindTransformMatrix[12]
                    this.m31 = bone.localBindTransformMatrix[13]
                    this.m32 = bone.localBindTransformMatrix[14]
                    this.m33 = bone.localBindTransformMatrix[15]
                }

            val modelMatrix = Matrix4f(parentMatrix).mul(thisMatrix)

            globalBindTransformsMatrices[name] = modelMatrix
        }

        val inverseBindPoseMatrices = mutableMapOf<String, Matrix4f>()
        for ((name, matrix) in globalBindTransformsMatrices) {
            inverseBindPoseMatrices[name] = Matrix4f(matrix).invert()
        }

        return Skeleton(
            skeleton,
            MeshRenderingState(gl, quadVAO, meshModel.faces.size * 9),
            skeletonGroupNamesToSkeletonGroupIds,
            globalBindTransformsMatrices,
            inverseBindPoseMatrices
        )
    }

    fun createFontAtlas(): MutableMap<Char, FontCharacter> {
        // positions: vec2, uv: vec2
        val input = "abcdefghijklmnopqrstuvwxyz0123456789:"
        val characters = mutableMapOf<Char, FontCharacter>()

        for ((index, char) in input.withIndex()) {
            val widthRelativeToSize = (7 / 512f)
            val heightRelativeToSize = (7 / 512f)

            val uvTopLeft = index * widthRelativeToSize
            val uvTopRight = (index + 1) * widthRelativeToSize

            characters[char] = FontCharacter(uvTopLeft, uvTopRight)
        }

        return characters
    }
}