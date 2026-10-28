package net.perfectdreams.gessyban.lwjgl

import kotlinx.coroutines.runBlocking
import net.perfectdreams.gessyban.GameResources
import net.perfectdreams.gessyban.Gessyban
import net.perfectdreams.gessyban.Stage
import net.perfectdreams.gessyban.VirtualFileSystem
import net.perfectdreams.gessyban.lwjgl.textures.LWJGLTextureManager
import net.perfectdreams.gessyban.meshes.JsonMeshManager
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.opengl.OpenGLContext
import net.perfectdreams.harmony.gl.opengl.OpenGLDebugging
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.GLFW_FALSE
import org.lwjgl.glfw.GLFW.GLFW_KEY_A
import org.lwjgl.glfw.GLFW.GLFW_KEY_D
import org.lwjgl.glfw.GLFW.GLFW_KEY_S
import org.lwjgl.glfw.GLFW.GLFW_KEY_W
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.lwjgl.glfw.GLFW.GLFW_RESIZABLE
import org.lwjgl.glfw.GLFW.GLFW_TRUE
import org.lwjgl.glfw.GLFW.GLFW_VISIBLE
import org.lwjgl.glfw.GLFW.glfwCreateWindow
import org.lwjgl.glfw.GLFW.glfwDefaultWindowHints
import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import org.lwjgl.glfw.GLFW.glfwGetCursorPos
import org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor
import org.lwjgl.glfw.GLFW.glfwGetVideoMode
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback
import org.lwjgl.glfw.GLFW.glfwSetErrorCallback
import org.lwjgl.glfw.GLFW.glfwSetKeyCallback
import org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback
import org.lwjgl.glfw.GLFW.glfwSetScrollCallback
import org.lwjgl.glfw.GLFW.glfwSetWindowPos
import org.lwjgl.glfw.GLFW.glfwShowWindow
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.glfw.GLFW.glfwSwapInterval
import org.lwjgl.glfw.GLFW.glfwTerminate
import org.lwjgl.glfw.GLFW.glfwWindowHint
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWScrollCallbackI
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.io.File
import java.nio.IntBuffer
import kotlin.use

class GessybanLWJGL {
    // The window handle
    private var window: Long = 0
    lateinit var gessyban: Gessyban

    fun start() {
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
            when (val stage = gessyban.stage) {
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
                    gessyban.zoom -= 1f
                } else if (0 > yoffset) {
                    gessyban.zoom += 1f
                }

                gessyban.zoom = gessyban.zoom.coerceIn(2f..8f)
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

                gessyban.stage.onClick(x, y)
            }
        }

        glfwSetCursorPosCallback(window) { window, xpos, ypos ->
            gessyban.stage.onCursorMove(xpos, ypos)
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

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()
        OpenGLDebugging.enableReportGLErrors()
        val gl = OpenGLContext()
        val gpu = GPUResources(gl, 1280, 720)
        val vfs = loadVirtualFileSystem(File("assets"))
        val textureManager = LWJGLTextureManager(gpu, vfs)

        // Set the app icon
        textureManager.setApplicationIcon(window, "textures/gessy_trauma.png")

        val resources = runBlocking { GameResources.load(gpu, textureManager, JsonMeshManager(),  vfs) }
        this.gessyban = Gessyban(gl, gpu, vfs, resources)

        // Set the clear color
        gl.clearColor(23 / 255f, 35 / 255f, 35 / 255f, 1.0f)

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            gessyban.render()

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    /**
     * Loads all files in the [assetsFolder] into a [VirtualFileSystem], keyed by their path relative to the [assetsFolder]
     */
    private fun loadVirtualFileSystem(assetsFolder: File): VirtualFileSystem {
        check(assetsFolder.isDirectory) { "Assets folder ${assetsFolder.absolutePath} does not exist!" }

        val vfs = VirtualFileSystem()
        assetsFolder.walkTopDown()
            .filter { it.isFile }
            .forEach {
                vfs.files[it.relativeTo(assetsFolder).invariantSeparatorsPath] = it.readBytes()
            }
        return vfs
    }
}
