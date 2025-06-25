package net.perfectdreams.gessyban.skeletons

import kotlinx.serialization.json.Json
import org.joml.Matrix4f
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

            val thisMatrix = Matrix4f()
                .m00(bone.localBindTransformMatrix[0])
                .m01(bone.localBindTransformMatrix[1])
                .m02(bone.localBindTransformMatrix[2])
                .m03(bone.localBindTransformMatrix[3])

                .m10(bone.localBindTransformMatrix[4])
                .m11(bone.localBindTransformMatrix[5])
                .m12(bone.localBindTransformMatrix[6])
                .m13(bone.localBindTransformMatrix[7])

                .m20(bone.localBindTransformMatrix[8])
                .m21(bone.localBindTransformMatrix[9])
                .m22(bone.localBindTransformMatrix[10])
                .m23(bone.localBindTransformMatrix[11])

                .m30(bone.localBindTransformMatrix[12])
                .m31(bone.localBindTransformMatrix[13])
                .m32(bone.localBindTransformMatrix[14])
                .m33(bone.localBindTransformMatrix[15])

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