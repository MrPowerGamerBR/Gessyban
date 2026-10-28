package net.perfectdreams.gessyban

import com.mrpowergamerbr.starfait.Vector2f
import net.perfectdreams.gessyban.meshes.JsonMeshManager
import net.perfectdreams.gessyban.skeletons.SkeletonManager
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.graphics.batcher.SpriteBatcher
import kotlin.random.Random

class Gessyban(
    val gl: GLContext,
    val gpu: GPUResources,
    val vfs: VirtualFileSystem,
    val gameResources: GameResources
) {
    private val skeletonManager = SkeletonManager()
    private val startedAt = currentTimeMillis()

    val random = Random.Default

    var zoom = 3f

    var stage: Stage = Stage.MainMenu(this)

    /**
     * Gets the time elapsed since the game has started, in seconds (like GLFW's `glfwGetTime()`)
     */
    fun getTime() = (currentTimeMillis() - startedAt) / 1000.0

    fun render() {
        stage.render()
    }

    fun renderText(
        batcher: SpriteBatcher.SpriteBatcherScope,
        position: Vector2f,
        size: Float,
        input: String,
    ) {
        var x = 0f

        for (char in input) {
            val character = gameResources.fontCharactersMeshes[char]
            if (character != null) {
                batcher.drawQuad(
                    position.x + x,
                    position.y,
                    size,
                    size,
                    character.uvTopLeft,
                    0f,
                    character.uvTopRight,
                    1f,
                    gameResources.fontTexture,
                    1f,
                    1f,
                    1f,
                    1f
                )
            }

            x += size
        }
    }
}