package net.perfectdreams.gessyban.skeletons

import kotlinx.serialization.Serializable

@Serializable
data class AnimationBone(
    val rotation: Rotation
) {
    @Serializable
    data class Rotation(
        val w: Float,
        val x: Float,
        val y: Float,
        val z: Float
    )
}