package net.perfectdreams.gessyban.skeletons

import com.mrpowergamerbr.starfait.Matrix4f
import net.perfectdreams.oldharmony.gl.meshes.MeshRenderingState

class Skeleton(
    val bones: Map<String, NewBone>,
    val meshRenderingState: MeshRenderingState,
    val groupNamesToGroupIds: Map<String, Int>,
    val globalBindTransformsMatrices: MutableMap<String, Matrix4f>,
    val inverseBindPoseMatrices: MutableMap<String, Matrix4f>
)