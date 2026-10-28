package net.perfectdreams.gessyban.skeletons

import com.mrpowergamerbr.starfait.Matrix4f
import kotlinx.serialization.json.Json
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class SkeletonManager {
    fun createSkeleton(content: String): Skeleton {
        val skeleton = Json.decodeFromString<Map<String, NewBone>>(content)

        val globalBindTransformsMatrices = mutableMapOf<String, Matrix4f>()

        for ((name, bone) in skeleton) {
            val parentMatrix = if (bone.parent == null) {
                Matrix4f()
            } else {
                globalBindTransformsMatrices[bone.parent] ?: error("Parent matrix not found for bone $name")
            }

            val thisMatrix = Matrix4f().apply {
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

        TODO()
    }
}