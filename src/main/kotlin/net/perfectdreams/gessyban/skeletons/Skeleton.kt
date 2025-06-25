package net.perfectdreams.gessyban.skeletons

import net.perfectdreams.harmony.gl.meshes.MeshRenderingState
import org.joml.Matrix4f

class Skeleton(
    val bones: Map<String, NewBone>,
    val meshRenderingState: MeshRenderingState,
    val groupNamesToGroupIds: Map<String, Int>,
    val globalBindTransformsMatrices: MutableMap<String, Matrix4f>,
    val inverseBindPoseMatrices: MutableMap<String, Matrix4f>
)