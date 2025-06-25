package net.perfectdreams.gessyban.skeletons

import kotlinx.serialization.Serializable

@Serializable
data class NewBone(
    val parent: String?,
    val localBindTransformMatrix: List<Float>,
)