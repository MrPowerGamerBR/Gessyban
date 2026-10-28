package net.perfectdreams.gessyban

import net.perfectdreams.gessyban.meshes.JsonMeshManager
import net.perfectdreams.gessyban.skeletons.SkeletonManager
import net.perfectdreams.harmony.gl.GLConstants.GL_ARRAY_BUFFER
import net.perfectdreams.harmony.gl.GLConstants.GL_STATIC_DRAW
import net.perfectdreams.harmony.gl.GLContext
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.oldharmony.gl.meshes.MeshRenderingState
import net.perfectdreams.harmony.gl.opengl.OpenGLContext
import net.perfectdreams.harmony.gl.opengl.OpenGLDebugging
import net.perfectdreams.harmony.gl.vertex.buildVertexAttributes
import net.perfectdreams.harmony.graphics.batcher.SpriteBatcher
import net.perfectdreams.oldharmony.gl.textures.TextureManager
import org.joml.Vector2f
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWScrollCallbackI
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.IntBuffer
import java.util.*


class Gessyban {
    // The window handle
    private var window: Long = 0
    val meshManager = JsonMeshManager()
    private val skeletonManager = SkeletonManager()
    lateinit var gl: GLContext
    lateinit var gpu: GPUResources
    lateinit var textureManager: TextureManager

    // Can only be loaded after creating GL Context
    lateinit var gameResources: GameResources
    val random = SplittableRandom()

    var zoom = 3f

    var stage: Stage = Stage.MainMenu(this)

    fun start() {
        println("Hello LWJGL " + Version.getVersion() + "!")

        init()

        loop()

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    private fun init() {
        val level = Integer.getInteger("gessyban.level", 1)
        // game.loadMapFromString(level, File("assets/maps/map$level.txt").readText())

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Create the window
        window = glfwCreateWindow(1280, 720, "Gessyban", NULL, NULL)
        if (window == NULL) throw RuntimeException("Failed to create the GLFW window")

        glfwSetKeyCallback(window) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            when (val stage = stage) {
                is Stage.MainMenu -> {}
                is Stage.Game -> {
                    if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
                        stage.game.movePlayer(1, 0)
                    }
                    if (key == GLFW_KEY_A && action == GLFW_RELEASE) {
                        stage.game.movePlayer(-1, 0)
                    }

                    if (key == GLFW_KEY_W && action == GLFW_RELEASE) {
                        stage.game.movePlayer(0, 1)
                    }
                    if (key == GLFW_KEY_S && action == GLFW_RELEASE) {
                        stage.game.movePlayer(0, -1)
                    }
                }
            }
        }

        glfwSetScrollCallback(window, object: GLFWScrollCallbackI {
            override fun invoke(window: Long, xoffset: Double, yoffset: Double) {
                if (yoffset > 0) {
                    zoom -= 1f
                } else if (0 > yoffset) {
                    zoom += 1f
                }

                zoom = zoom.coerceIn(2f..8f)
            }
        })

        glfwSetMouseButtonCallback(window) { window: Long, button: Int, action: Int, mods: Int ->
            if (action == GLFW_PRESS) {
                val xpos = DoubleArray(1)
                val ypos = DoubleArray(1)

                // Get the cursor position relative to the window
                glfwGetCursorPos(window, xpos, ypos)

                val x = xpos[0]
                val y = ypos[0]

                stage.onClick(x, y)
            }
        }

        glfwSetCursorPosCallback(window) { window, xpos, ypos ->
            stage.onCursorMove(xpos, ypos)
        }

        stackPush().use { stack ->
            val pWidth: IntBuffer = stack.mallocInt(1) // int*
            val pHeight: IntBuffer = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode: GLFWVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            )
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)
    }

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()
        OpenGLDebugging.enableReportGLErrors()
        val gl = OpenGLContext()
        val gpu = GPUResources(gl, 1280, 720)
        textureManager = TextureManager(gpu)
        this.gl = gl
        this.gpu = gpu

        // Set the app icon
        textureManager.setApplicationIcon(
            window,
            "assets/textures/gessy_trauma.png",
            false
        )

        gameResources = GameResources(this)

        // Set the clear color
        gl.clearColor(23 / 255f, 35 / 255f, 35 / 255f, 1.0f)

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.

        while (!glfwWindowShouldClose(window)) {
            stage.render()

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }
    }

    fun setupScreenQuad(): MeshRenderingState {
        // positions: vec2, uv: vec2
        val quadVertices = floatArrayOf(
            // The descriptions are as a full quad
            // If we do a triangle from 0f to 1f, everything will be positioned around the quad's top left corner

            // First triangle
            0.0f, 0.0f, 0.0f, 1.0f, // top left
            1.0f, 0.0f, 1.0f, 1.0f, // top right
            1.0f, 1.0f, 1.0f, 0.0f, // bottom right

            // Second triangle
            1.0f, 1.0f, 1.0f, 0.0f, // bottom right
            0.0f, 1.0f, 0.0f, 0.0f, // bottom left
            0.0f, 0.0f, 0.0f, 1.0f, // top left
        )

        val screenQuadVao = gl.genVertexArrays()
        val vbo = gl.genBuffers()

        gl.bindVertexArray(screenQuadVao)
        gl.bindBuffer(GL_ARRAY_BUFFER, vbo)
        gl.bufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW)

        // Index: The index of the vertex attribute
        // Size: The size of each entry
        // Type: The type
        // Normalized: Whether the data should be normalized
        // Stride: The byte offset between consecutive attributes
        // Pointer: Where the data is in the array

        // So, as an example...
        // For something with size 2 (two floats) in a array that has vec2 (pos), vec2 (uv)
        // If we want to access the second vec2, we would need to...
        // size = 2 (because each entry is 2 floats)
        // stride = 4 * Float.SIZE_BYTES (because each entry is [entry], *8 bytes for the pos*, [entry] ...), the stride is relative to the START of the vertex!
        // pointer = the data starts at 2 * Float.SIZE_BYTES

        // Position attribute
        val attributes = buildVertexAttributes {
            vec2(0) // position
            vec2(1) // uv
        }
        attributes.apply(gl)

        return MeshRenderingState(gl, screenQuadVao, 6)
    }

    /**
     * Performs linear interpolation between two values.
     *
     * @param start The starting value
     * @param end The ending value
     * @param fraction A value between 0 and 1 representing the interpolation point
     * @return The interpolated value
     */
    fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
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