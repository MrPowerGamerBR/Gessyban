package net.perfectdreams.gessyban

import net.perfectdreams.gessyban.framebuffers.TextureDumper
import net.perfectdreams.harmony.gl.shaders.bind
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.opengl.GL11.glClearColor
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.GL_TEXTURE1
import org.lwjgl.opengl.GL13.GL_TEXTURE2
import org.lwjgl.opengl.GL13.GL_TEXTURE3
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL30.glBindFramebuffer
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.math.sin

sealed class Stage(val m: Gessyban) {
    abstract fun render()

    abstract fun onClick(x: Double, y: Double)
    abstract fun onCursorMove(x: Double, y: Double)

    class MainMenu(m: Gessyban) : Stage(m) {
        var isHoveringButton = false
        var startedFadeAt: Double? = null

        override fun render() {
            val fadeProgress = if (this.startedFadeAt != null) {
                (glfwGetTime().toFloat() - startedFadeAt!!.toFloat()) * 2
            } else {
                0.0f
            }

            if (fadeProgress >= 1.0) {
                val gameStage = Game(m)
                gameStage.game.loadMapFromString(1, File("assets/maps/map1.txt").readText())

                m.stage = gameStage
            }

            m.gameResources.menuFramebuffer.bind {
                glDisable(GL_DEPTH_TEST)
                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                val width = 1280f
                val height = 720f

                val halfWidth = width / 2
                val halfHeight = height / 2

                m.gameResources.shader2d.bind {
                    this.uModel.set(
                        Matrix4f()
                            .translate(halfWidth - ((52f * 16) / 2f), 80f + (20f * sin((glfwGetTime()).toFloat())), 0f)
                            .scale(52f * 16, 11f * 16, 1f)
                            .get(FloatArray(16))
                    )
                    this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.uView.set(Matrix4f().get(FloatArray(16)))
                    this.uTexture.set(GL_TEXTURE0, m.gameResources.logoTexture)

                    m.gameResources.screenQuadMesh.render()
                }

                val buttonPosition = Vector2f(halfWidth - ((38f * 8) / 2f), 400f)

                m.gameResources.shader2d.bind {
                    this.uModel.set(
                        Matrix4f()
                            .translate(buttonPosition.x, buttonPosition.y, 0f)
                            .scale(38f * 8, 11f * 8, 1f)
                            .get(FloatArray(16))
                    )
                    this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.uView.set(Matrix4f().get(FloatArray(16)))
                    if (isHoveringButton) {
                        this.uTexture.set(GL_TEXTURE0, m.gameResources.startButtonHoverTexture)
                    } else {
                        this.uTexture.set(GL_TEXTURE0, m.gameResources.startButtonTexture)
                    }

                    m.gameResources.screenQuadMesh.render()
                }

                glEnable(GL_DEPTH_TEST)

                val viewMatrix = Matrix4f()
                    .translate(0f, 0f, 0f)

                glClear(GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                m.gameResources.shader3d.bind {
                    this.uIsObjectIdPass.set(false)
                    this.uUseNormalsAsColor.set(false)

                    this.uView.set(viewMatrix.get(FloatArray(16)))
                    this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.uModel.set(
                        Matrix4f()
                            .translate(1200f, 1100f, 0f)
                            .scale(-400f, 400f, 1f)
                            .rotateZ(Math.toRadians(200.0).toFloat())
                            // .translate(0f, -2f, 0f)
                            .get(FloatArray(16))
                    )
                    this.uTexture.set(GL_TEXTURE0, m.gameResources.gessyTexture)
                    this.uObjectIdRGB.set(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())

                    // Transformed matrices for the pose
                    val globalAnimatedTransformsMatrices = mutableMapOf<String, Matrix4f>()

                    for ((name, bone) in m.gameResources.gessySkeleton.bones) {
                        val parentMatrix = if (bone.parent == null) {
                            Matrix4f()
                        } else {
                            globalAnimatedTransformsMatrices[bone.parent]
                                ?: error("Parent matrix not found for bone $name")
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

                        val animBones = m.gameResources.gessyHiPose[name]!!

                        modelMatrix.rotate(
                            Quaternionf(
                                animBones.rotation.x,
                                animBones.rotation.y,
                                animBones.rotation.z,
                                animBones.rotation.w
                            )
                        )

                        globalAnimatedTransformsMatrices[name] = modelMatrix
                    }

                    val boneTransformsMatrixArray = FloatArray(19 * 16)

                    for ((groupName, index) in m.gameResources.gessySkeleton.groupNamesToGroupIds) {
                        Matrix4f(globalAnimatedTransformsMatrices[groupName]!!)
                            .mul(m.gameResources.gessySkeleton.inverseBindPoseMatrices[groupName]!!)
                            .get(boneTransformsMatrixArray, index * 16)
                    }

                    this.uBoneMatrices.set(boneTransformsMatrixArray)

                    m.gameResources.gessySkeleton.meshRenderingState.render()
                }
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glViewport(0, 0, 1280, 720)

            glEnable(GL_DEPTH_TEST)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            m.gameResources.shaderPixelFade.bind {
                this.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                this.uView.set(Matrix4f().get(FloatArray(16)))
                this.uScreenTexture.set(GL_TEXTURE0, m.gameResources.menuFramebuffer.textureId)
                this.uFadeAmount.set(fadeProgress)

                m.gameResources.screenQuadMesh.render()
            }
        }

        override fun onClick(x: Double, y: Double) {
            val width = 1280f
            val halfWidth = width / 2
            val buttonPositionTopLeft = Vector2f(halfWidth - ((38f * 8) / 2f), 400f)
            val buttonPositionBottomRight = Vector2f(buttonPositionTopLeft.x + (38f * 8), buttonPositionTopLeft.y + (11f * 8))

            println("Top Left is: ${buttonPositionTopLeft.x}, ${buttonPositionTopLeft.y}")
            println("Bottom Right is: ${buttonPositionBottomRight.x}, ${buttonPositionBottomRight.y}")

            if (x in buttonPositionTopLeft.x..buttonPositionBottomRight.x && y in buttonPositionTopLeft.y..buttonPositionBottomRight.y && startedFadeAt == null) {
                startedFadeAt = glfwGetTime()
            }
        }

        override fun onCursorMove(x: Double, y: Double) {
            val width = 1280f
            val halfWidth = width / 2
            val buttonPositionTopLeft = Vector2f(halfWidth - ((38f * 8) / 2f), 400f)
            val buttonPositionBottomRight = Vector2f(buttonPositionTopLeft.x + (38f * 8), buttonPositionTopLeft.y + (11f * 8))

            if (x in buttonPositionTopLeft.x..buttonPositionBottomRight.x && y in buttonPositionTopLeft.y..buttonPositionBottomRight.y) {
                isHoveringButton = true
            } else {
                isHoveringButton = false
            }
        }
    }

    class Game(m: Gessyban) : Stage(m) {
        val game = SokobanGame(m)
        val wallColor = Vector3f(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())
        val floorColor = Vector3f(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())
        val startedAt = glfwGetTime()

        override fun render() {
            val aspectRatio = 1280f / 720f

            // Projection matrix (perspective projection)
            val projectionMatrix = Matrix4f().ortho(
                -aspectRatio * m.zoom,
                aspectRatio * m.zoom,
                m.zoom,
                -m.zoom,
                -100.0f, // Near plane
                100.0f // Far plane
            )

            val player = game.tiles.first { it is Tile.Player } as Tile.Player

            fun renderScene(
                isObjectIdPass: Boolean,
                isNormalPass: Boolean
            ) {
                if (isObjectIdPass && isNormalPass)
                    error("Invalid pass combination!")

                val viewMatrix = Matrix4f()
                    .rotate(Math.toRadians(130.0).toFloat(), 1f, 0f, 0f)
                    .rotate(Math.toRadians(20.0).toFloat(), 0f, 1f, 0f)
                    .translate(-(player.x.toFloat() + 0.5f), 0f, -(player.z.toFloat() - 0.5f))

                m.gameResources.shader3d.bind {
                    if (isObjectIdPass) {
                        this.uIsObjectIdPass.set(true)
                    } else {
                        this.uIsObjectIdPass.set(false)
                    }

                    if (isNormalPass) {
                        this.uUseNormalsAsColor.set(true)
                    } else {
                        this.uUseNormalsAsColor.set(false)
                    }

                    for (x in 0 until game.playfieldWidth) {
                        for (z in 0 until game.playfieldHeight) {
                            this.uView.set(viewMatrix.get(FloatArray(16)))
                            this.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.uModel.set(
                                Matrix4f()
                                    .translate(1f * x, 0f, 1f * z)
                                    .translate(0.5f, 0.0f, -0.5f)
                                    .get(FloatArray(16))
                            )

                            this.uTexture.set(GL_TEXTURE0, m.gameResources.floorTexture)

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.uBoneMatrices.set(boneMatrices)
                            this.uObjectIdRGB.set(floorColor.x, floorColor.y, floorColor.z)

                            m.gameResources.floorMesh.render()
                        }
                    }

                    for (tile in game.tiles) {
                        if (tile is Tile.Wall) {
                            this.uView.set(viewMatrix.get(FloatArray(16)))
                            this.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.uModel.set(
                                Matrix4f()
                                    .translate(1f * tile.x, 0f, 1f * tile.z)
                                    .translate(0.5f, 0.0f, -0.5f)
                                    .get(FloatArray(16))
                            )
                            this.uTexture.set(GL_TEXTURE0, m.gameResources.wallTexture)
                            this.uObjectIdRGB.set(wallColor.x, wallColor.y, wallColor.z)

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.uBoneMatrices.set(boneMatrices)

                            m.gameResources.wallMesh.render()
                        } else if (tile is Tile.Box) {
                            this.uView.set(viewMatrix.get(FloatArray(16)))
                            this.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.uModel.set(
                                Matrix4f()
                                    .translate(1f * tile.x, 0f, 1f * tile.z)
                                    .translate(0.5f, 0.0f, -0.5f)
                                    .get(FloatArray(16))
                            )
                            this.uTexture.set(GL_TEXTURE0, m.gameResources.boxTexture)
                            this.uObjectIdRGB.set(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.uBoneMatrices.set(boneMatrices)

                            // heh, it uses the same mesh xd
                            m.gameResources.wallMesh.render()
                        } else if (tile is Tile.Player) {
                            this.uView.set(viewMatrix.get(FloatArray(16)))
                            this.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.uModel.set(
                                Matrix4f()
                                    .translate(1f * tile.x, 0f, 1f * tile.z)
                                    .translate(0.5f, 0.0f, -0.5f)
                                    .apply {
                                        when (player.lastMove) {
                                            Tile.Player.LastMove.UP -> rotateY(Math.toRadians(0.0).toFloat())
                                            Tile.Player.LastMove.DOWN -> rotateY(Math.toRadians(180.0).toFloat())
                                            Tile.Player.LastMove.LEFT -> rotateY(Math.toRadians(270.0).toFloat())
                                            Tile.Player.LastMove.RIGHT -> rotateY(Math.toRadians(90.0).toFloat())
                                        }
                                    }
                                    .get(FloatArray(16))
                            )
                            this.uTexture.set(GL_TEXTURE0, m.gameResources.gessyTexture)
                            this.uObjectIdRGB.set(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())

                            // Transformed matrices for the pose
                            val globalAnimatedTransformsMatrices = mutableMapOf<String, Matrix4f>()

                            for ((name, bone) in m.gameResources.gessySkeleton.bones) {
                                val parentMatrix = if (bone.parent == null) {
                                    Matrix4f()
                                } else {
                                    globalAnimatedTransformsMatrices[bone.parent]
                                        ?: error("Parent matrix not found for bone $name")
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

                                val animBones = if (!tile.hasMovedAtLeastOnce)
                                    m.gameResources.gessyIdlePose[name]!!
                                else
                                    m.gameResources.gessyWalkPose[name]!!

                                modelMatrix.rotate(
                                    Quaternionf(
                                        animBones.rotation.x,
                                        animBones.rotation.y,
                                        animBones.rotation.z,
                                        animBones.rotation.w
                                    )
                                )

                                globalAnimatedTransformsMatrices[name] = modelMatrix
                            }

                            val boneTransformsMatrixArray = FloatArray(19 * 16)

                            for ((groupName, index) in m.gameResources.gessySkeleton.groupNamesToGroupIds) {
                                Matrix4f(globalAnimatedTransformsMatrices[groupName]!!)
                                    .mul(m.gameResources.gessySkeleton.inverseBindPoseMatrices[groupName]!!)
                                    .get(boneTransformsMatrixArray, index * 16)
                            }

                            this.uBoneMatrices.set(boneTransformsMatrixArray)

                            m.gameResources.gessySkeleton.meshRenderingState.render()
                        } else {
                            this.uView.set(viewMatrix.get(FloatArray(16)))
                            this.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.uModel.set(
                                Matrix4f()
                                    .translate(1f * tile.x, 0.0001f, 1f * tile.z)
                                    .scale(1f, 1f, 1f)
                                    .rotateX(Math.toRadians(-90.0).toFloat())
                                    .get(FloatArray(16))
                            )

                            when (tile) {
                                is Tile.Box -> {
                                    this.uTexture.set(GL_TEXTURE0, m.gameResources.boxTexture)
                                }

                                is Tile.Player -> {
                                    this.uTexture.set(GL_TEXTURE0, m.gameResources.furalhaTexture)
                                }

                                is Tile.PressurePlate -> {
                                    this.uTexture.set(GL_TEXTURE0, m.gameResources.pressurePlateTexture)
                                }

                                is Tile.Wall -> {
                                    this.uTexture.set(GL_TEXTURE0, m.gameResources.wallTexture)
                                }
                            }

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.uBoneMatrices.set(boneMatrices)
                            this.uObjectIdRGB.set(floorColor.x, floorColor.y, floorColor.z)

                            m.gameResources.screenQuadMesh.render()
                        }
                    }
                }
            }

            m.gameResources.gameFramebuffer.bind {
                glEnable(GL_DEPTH_TEST)
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                renderScene(false, false)
            }

            m.gameResources.objectIdFramebuffer.bind {
                glEnable(GL_DEPTH_TEST)
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                renderScene(true, false)
            }

            m.gameResources.normalsFramebuffer.bind {
                glEnable(GL_DEPTH_TEST)
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                renderScene(false, true)
            }

            m.gameResources.pixelFilterFramebuffer.bind {
                glEnable(GL_DEPTH_TEST)
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                m.gameResources.shaderPixelFilter.bind {
                    this.uModel.set(Matrix4f().scale(320f, 180f, 1f).get(FloatArray(16)))
                    this.uProjection.set(m.gameResources.pixelProjectionMatrix.get(FloatArray(16)))
                    this.uView.set(Matrix4f().get(FloatArray(16)))
                    this.uScreenTexture.set(GL_TEXTURE0, m.gameResources.gameFramebuffer.textureId)
                    this.uDepthTexture.set(GL_TEXTURE1, m.gameResources.gameFramebuffer.depthTextureId)
                    this.uUniqueObjectIdTexture.set(GL_TEXTURE2, m.gameResources.objectIdFramebuffer.textureId)
                    this.uNormalsTexture.set(GL_TEXTURE3, m.gameResources.normalsFramebuffer.textureId)

                    m.gameResources.screenQuadMesh.render()
                }
            }

            m.gameResources.guiFramebuffer.bind {
                glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                m.renderText(Vector2f(0f, 0f), 7f * 10f, "level ${game.currentLevel}")
                m.renderText(Vector2f(0f, 7f * 10f), 7f * 10f, "x: ${player.x}")
                m.renderText(Vector2f(0f, (7f * 10f) * 2), 7f * 10f, "z: ${player.z}")
            }

            m.gameResources.upscaledFramebuffer.bind {
                m.gameResources.shader2d.bind {
                    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                    glEnable(GL_DEPTH_TEST)
                    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                    this.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                    this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.uView.set(Matrix4f().get(FloatArray(16)))
                    this.uTexture.set(GL_TEXTURE0, m.gameResources.pixelFilterFramebuffer.textureId)

                    m.gameResources.screenQuadMesh.render()

                    glClear(GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                    this.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                    this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.uView.set(Matrix4f().get(FloatArray(16)))
                    this.uTexture.set(GL_TEXTURE0, m.gameResources.guiFramebuffer.textureId)

                    m.gameResources.screenQuadMesh.render()
                }
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0)
            glViewport(0, 0, 1280, 720)

            glEnable(GL_DEPTH_TEST)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            m.gameResources.shaderPixelFade.bind {
                this.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                this.uView.set(Matrix4f().get(FloatArray(16)))
                this.uScreenTexture.set(GL_TEXTURE0, m.gameResources.upscaledFramebuffer.textureId)
                this.uFadeAmount.set((1.0f - ((glfwGetTime() - startedAt).toFloat() * 2.0f)).coerceAtLeast(0.0f))

                m.gameResources.screenQuadMesh.render()
            }

            /* shader2d.bind {
                val player = game.tiles.first { it is Tile.Player } as Tile.Player
                val halfWidth = ((1280f / 2) - 64f) - (player.x * 128f)
                val halfHeight = ((720f / 2) - 64f) - (player.z * 128f)

                for (tile in game.tiles) {
                    this.uModel.set(
                        Matrix4f().translate(128f * tile.x, 128f * tile.z, 0f).scale(128f, 128f, 1f).get(FloatArray(16))
                    )
                    this.uView.set(Matrix4f().translate(halfWidth, halfHeight, 0f).get(FloatArray(16)))
                    this.uProjection.set(screenProjectionMatrix.get(FloatArray(16)))

                    when (tile) {
                        is Tile.Box -> {
                            this.uTexture.set(GL_TEXTURE0, boxTexture)
                        }
                        is Tile.Player -> {
                            this.uTexture.set(GL_TEXTURE0, furalhaTexture)
                        }
                        is Tile.PressurePlate -> {
                            this.uTexture.set(GL_TEXTURE0, pressurePlateTexture)
                        }
                        is Tile.Wall -> {
                            this.uTexture.set(GL_TEXTURE0, wallTexture)
                        }
                    }


                    screenQuadMesh.render()
                }
            } */

            /* TextureDumper.dumpTexture(
                m.gameResources.gameFramebuffer.textureId,
                m.gameResources.gameFramebuffer.width,
                m.gameResources.gameFramebuffer.height,
                "fb1.png"
            )

            TextureDumper.dumpTexture(
                m.gameResources.objectIdFramebuffer.textureId,
                m.gameResources.objectIdFramebuffer.width,
                m.gameResources.objectIdFramebuffer.height,
                "fb2.png"
            )

            TextureDumper.dumpTexture(
                m.gameResources.normalsFramebuffer.textureId,
                m.gameResources.normalsFramebuffer.width,
                m.gameResources.normalsFramebuffer.height,
                "fb3.png"
            )

            TextureDumper.dumpTexture(
                m.gameResources.pixelFilterFramebuffer.textureId,
                m.gameResources.pixelFilterFramebuffer.width,
                m.gameResources.pixelFilterFramebuffer.height,
                "fb4.png"
            ) */
        }

        override fun onClick(x: Double, y: Double) {
            game.loadMapFromString(game.currentLevel, File("assets/maps/map${game.currentLevel}.txt").readText())
        }

        override fun onCursorMove(x: Double, y: Double) {
        }
    }
}