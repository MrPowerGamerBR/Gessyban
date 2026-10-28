package net.perfectdreams.gessyban

import net.perfectdreams.harmony.gl.GLConstants.GL_BLEND
import net.perfectdreams.harmony.gl.GLConstants.GL_COLOR_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_DEPTH_BUFFER_BIT
import net.perfectdreams.harmony.gl.GLConstants.GL_DEPTH_TEST
import net.perfectdreams.harmony.gl.GLConstants.GL_ONE_MINUS_SRC_ALPHA
import net.perfectdreams.harmony.gl.GLConstants.GL_SRC_ALPHA
import net.perfectdreams.harmony.gl.framebuffers.withFramebuffer
import net.perfectdreams.harmony.gl.pipelines.Pipeline
import net.perfectdreams.harmony.gl.rendertarget.RenderTargetScope
import net.perfectdreams.harmony.gl.shaders.withProgram
import net.perfectdreams.harmony.graphics.batcher.withSpritePipeline
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.glfwGetTime
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

            m.gpu.onRenderTarget(m.gameResources.menuFramebuffer) {
                m.gl.enable(GL_BLEND)
                m.gl.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                val width = 1280f
                val height = 720f

                val halfWidth = width / 2
                val halfHeight = height / 2

                val buttonPosition = Vector2f(halfWidth - ((38f * 8) / 2f), 400f)

                withSpritePipeline(m.gameResources.spriteBatcher, m.gameResources.shader2d) {
                    this.shader.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))

                    it.drawBatch(this) {
                        drawQuad(
                            halfWidth - ((52f * 16) / 2f),
                            80f + (20f * sin((glfwGetTime()).toFloat())),
                            52f * 16f,
                            11f * 16f,
                            0f,
                            0f,
                            1f,
                            1f,
                            m.gameResources.logoTexture
                        )

                        drawQuad(
                            buttonPosition.x,
                            buttonPosition.y,
                            38f * 8,
                            11f * 8,
                            0f,
                            0f,
                            1f,
                            1f,
                            if (isHoveringButton) {
                                m.gameResources.startButtonHoverTexture
                            } else {
                                m.gameResources.startButtonTexture
                            }
                        )
                    }
                }

                val viewMatrix = Matrix4f()
                    .translate(0f, 0f, 0f)

                withPipeline(Pipeline(m.gameResources.shader3d, enableDepthTest = true)) {
                    this.shader.uIsObjectIdPass.set(false)
                    this.shader.uUseNormalsAsColor.set(false)

                    this.shader.uView.set(viewMatrix.get(FloatArray(16)))
                    this.shader.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.shader.uModel.set(
                        Matrix4f()
                            .translate(1200f, 1100f, 0f)
                            .scale(-400f, 400f, 1f)
                            .rotateZ(Math.toRadians(200.0).toFloat())
                            // .translate(0f, -2f, 0f)
                            .get(FloatArray(16))
                    )
                    this.shader.uTexture.set(0, m.gameResources.gessyTexture)
                    this.shader.uObjectIdRGB.set(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())

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

                    this.shader.uBoneMatrices.set(boneTransformsMatrixArray)

                    m.gameResources.gessySkeleton.meshRenderingState.render()
                }
            }

            m.gpu.onRenderTarget(m.gpu.windowRenderTarget) {
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                withPipeline(Pipeline(m.gameResources.shaderPixelFade, enableDepthTest = true)) {
                    this.shader.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                    this.shader.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.shader.uView.set(Matrix4f().get(FloatArray(16)))
                    this.shader.uScreenTexture.set(0, m.gameResources.menuFramebuffer.texture)
                    this.shader.uFadeAmount.set(fadeProgress)

                    m.gameResources.screenQuadMesh.render()
                }
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

            fun RenderTargetScope.renderScene(
                isObjectIdPass: Boolean,
                isNormalPass: Boolean
            ) {
                if (isObjectIdPass && isNormalPass)
                    error("Invalid pass combination!")

                val viewMatrix = Matrix4f()
                    .rotate(Math.toRadians(130.0).toFloat(), 1f, 0f, 0f)
                    .rotate(Math.toRadians(20.0).toFloat(), 0f, 1f, 0f)
                    .translate(-(player.x.toFloat() + 0.5f), 0f, -(player.z.toFloat() - 0.5f))

                withPipeline(Pipeline(m.gameResources.shader3d, enableDepthTest = true)) {
                    this.shader.uIsObjectIdPass.set(isObjectIdPass)
                    this.shader.uUseNormalsAsColor.set(isNormalPass)

                    for (x in 0 until game.playfieldWidth) {
                        for (z in 0 until game.playfieldHeight) {
                            this.shader.uView.set(viewMatrix.get(FloatArray(16)))
                            this.shader.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.shader.uModel.set(
                                Matrix4f()
                                    .translate(1f * x, 0f, 1f * z)
                                    .translate(0.5f, 0.0f, -0.5f)
                                    .get(FloatArray(16))
                            )

                            this.shader.uTexture.set(0, m.gameResources.floorTexture)

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.shader.uBoneMatrices.set(boneMatrices)
                            this.shader.uObjectIdRGB.set(floorColor.x, floorColor.y, floorColor.z)

                            m.gameResources.floorMesh.render()
                        }
                    }

                    for (tile in game.tiles) {
                        if (tile is Tile.Wall) {
                            this.shader.uView.set(viewMatrix.get(FloatArray(16)))
                            this.shader.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.shader.uModel.set(
                                Matrix4f()
                                    .translate(1f * tile.x, 0f, 1f * tile.z)
                                    .translate(0.5f, 0.0f, -0.5f)
                                    .get(FloatArray(16))
                            )
                            this.shader.uTexture.set(0, m.gameResources.wallTexture)
                            this.shader.uObjectIdRGB.set(wallColor.x, wallColor.y, wallColor.z)

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.shader.uBoneMatrices.set(boneMatrices)

                            m.gameResources.wallMesh.render()
                        } else if (tile is Tile.Box) {
                            this.shader.uView.set(viewMatrix.get(FloatArray(16)))
                            this.shader.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.shader.uModel.set(
                                Matrix4f()
                                    .translate(1f * tile.x, 0f, 1f * tile.z)
                                    .translate(0.5f, 0.0f, -0.5f)
                                    .get(FloatArray(16))
                            )
                            this.shader.uTexture.set(0, m.gameResources.boxTexture)
                            this.shader.uObjectIdRGB.set(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.shader.uBoneMatrices.set(boneMatrices)

                            // heh, it uses the same mesh xd
                            m.gameResources.wallMesh.render()
                        } else if (tile is Tile.Player) {
                            this.shader.uView.set(viewMatrix.get(FloatArray(16)))
                            this.shader.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.shader.uModel.set(
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
                            this.shader.uTexture.set(0, m.gameResources.gessyTexture)
                            this.shader.uObjectIdRGB.set(m.random.nextFloat(), m.random.nextFloat(), m.random.nextFloat())

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

                            this.shader.uBoneMatrices.set(boneTransformsMatrixArray)

                            m.gameResources.gessySkeleton.meshRenderingState.render()
                        } else {
                            this.shader.uView.set(viewMatrix.get(FloatArray(16)))
                            this.shader.uProjection.set(projectionMatrix.get(FloatArray(16)))
                            this.shader.uModel.set(
                                Matrix4f()
                                    .translate(1f * tile.x, 0.0001f, 1f * tile.z)
                                    .scale(1f, 1f, 1f)
                                    .rotateX(Math.toRadians(-90.0).toFloat())
                                    .get(FloatArray(16))
                            )

                            when (tile) {
                                is Tile.Box -> {
                                    this.shader.uTexture.set(0, m.gameResources.boxTexture)
                                }

                                is Tile.Player -> {
                                    this.shader.uTexture.set(0, m.gameResources.furalhaTexture)
                                }

                                is Tile.PressurePlate -> {
                                    this.shader.uTexture.set(0, m.gameResources.pressurePlateTexture)
                                }

                                is Tile.Wall -> {
                                    this.shader.uTexture.set(0, m.gameResources.wallTexture)
                                }
                            }

                            val boneMatrices = FloatArray(19 * 16)
                            repeat(19) {
                                Matrix4f().get(boneMatrices, it * 16)
                            }

                            this.shader.uBoneMatrices.set(boneMatrices)
                            this.shader.uObjectIdRGB.set(floorColor.x, floorColor.y, floorColor.z)

                            m.gameResources.screenQuadMesh.render()
                        }
                    }
                }
            }

            m.gpu.onRenderTarget(m.gameResources.gameFramebuffer) {
                m.gl.enable(GL_DEPTH_TEST)
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                renderScene(false, false)
            }

            m.gpu.onRenderTarget(m.gameResources.objectIdFramebuffer) {
                m.gl.enable(GL_DEPTH_TEST)
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                renderScene(true, false)
            }

            m.gpu.onRenderTarget(m.gameResources.normalsFramebuffer) {
                m.gl.enable(GL_DEPTH_TEST)
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                renderScene(false, true)
            }

            m.gpu.onRenderTarget(m.gameResources.pixelFilterFramebuffer) {
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                withPipeline(Pipeline(m.gameResources.shaderPixelFilter, enableDepthTest = true)) {
                    this.shader.uModel.set(Matrix4f().scale(320f, 180f, 1f).get(FloatArray(16)))
                    this.shader.uProjection.set(m.gameResources.pixelProjectionMatrix.get(FloatArray(16)))
                    this.shader.uView.set(Matrix4f().get(FloatArray(16)))
                    this.shader.uScreenTexture.set(10, m.gameResources.gameFramebuffer.texture)
                    this.shader.uDepthTexture.set(1, m.gameResources.gameFramebuffer.depthTexture!!)
                    this.shader.uUniqueObjectIdTexture.set(2, m.gameResources.objectIdFramebuffer.texture)
                    this.shader.uNormalsTexture.set(3, m.gameResources.normalsFramebuffer.texture)

                    m.gameResources.screenQuadMesh.render()
                }
            }

            m.gpu.onRenderTarget(m.gameResources.guiFramebuffer) {
                m.gl.clearColor(0.0f, 0.0f, 0.0f, 0.0f)
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                withSpritePipeline(m.gameResources.spriteBatcher, m.gameResources.shader2d) {
                    this.shader.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))

                    it.drawBatch(this) {
                        m.renderText(this, Vector2f(0f, 0f), 7f * 10f, "level ${game.currentLevel}")
                        m.renderText(this, Vector2f(0f, 7f * 10f), 7f * 10f, "x: ${player.x}")
                        m.renderText(this, Vector2f(0f, (7f * 10f) * 2), 7f * 10f, "z: ${player.z}")
                    }
                }
            }

            m.gpu.onRenderTarget(m.gameResources.upscaledFramebuffer) {
                m.gl.disable(GL_DEPTH_TEST)
                m.gl.clear(GL_COLOR_BUFFER_BIT)

                withSpritePipeline(m.gameResources.spriteBatcher, m.gameResources.shader2d) {
                    this.shader.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))

                    it.drawBatch(this) {
                        drawQuad(0f, 0f, 1280f, 720f, 0f, 1f, 1f, 0f, m.gameResources.pixelFilterFramebuffer.texture, 1f, 1f, 1f, 1f)
                        drawQuad(0f, 0f, 1280f, 720f, 0f, 1f, 1f, 0f, m.gameResources.guiFramebuffer.texture, 1f, 1f, 1f, 1f)
                    }
                }
                /* m.gameResources.shader2d.bind {
                    glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                    glEnable(GL_DEPTH_TEST)
                    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                    this.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                    this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.uView.set(Matrix4f().get(FloatArray(16)))
                    this.uTexture.set(0, m.gameResources.pixelFilterFramebuffer.textureId)

                    m.gameResources.screenQuadMesh.render()

                    glClear(GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                    this.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                    this.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.uView.set(Matrix4f().get(FloatArray(16)))
                    this.uTexture.set(0, m.gameResources.guiFramebuffer.texture.textureId)

                    m.gameResources.screenQuadMesh.render()
                } */
            }

            m.gpu.onRenderTarget(m.gpu.windowRenderTarget) {
                m.gl.clear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

                withPipeline(Pipeline(m.gameResources.shaderPixelFade, enableDepthTest = true)) {
                    this.shader.uModel.set(Matrix4f().scale(1280f, 720f, 1f).get(FloatArray(16)))
                    this.shader.uProjection.set(m.gameResources.screenProjectionMatrix.get(FloatArray(16)))
                    this.shader.uView.set(Matrix4f().get(FloatArray(16)))
                    this.shader.uScreenTexture.set(0, m.gameResources.upscaledFramebuffer.texture)
                    this.shader.uFadeAmount.set((1.0f - ((glfwGetTime() - startedAt).toFloat() * 2.0f)).coerceAtLeast(0.0f))

                    m.gameResources.screenQuadMesh.render()
                }
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
                            this.uTexture.set(0, boxTexture)
                        }
                        is Tile.Player -> {
                            this.uTexture.set(0, furalhaTexture)
                        }
                        is Tile.PressurePlate -> {
                            this.uTexture.set(0, pressurePlateTexture)
                        }
                        is Tile.Wall -> {
                            this.uTexture.set(0, wallTexture)
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