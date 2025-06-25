package net.perfectdreams.gessyban.meshes

import kotlinx.serialization.Serializable

@Serializable
data class JsonMesh(
    val groups: List<String>,
    val faces: List<Face>,
    val vertices: List<Vertex>
) {
    @Serializable
    data class Face(
        val v0: Int,
        val v1: Int,
        val v2: Int,
        val uv0: TextureCoordinate,
        val uv1: TextureCoordinate,
        val uv2: TextureCoordinate,
        val normal: Normal
    )

    @Serializable
    data class Vertex(
        val x: Float,
        val y: Float,
        val z: Float,
        val vertexGroups: List<String>
    )

    @Serializable
    data class TextureCoordinate(
        val x: Float,
        val y: Float
    )

    @Serializable
    data class Normal(
        val x: Float,
        val y: Float,
        val z: Float
    )
}