package net.perfectdreams.gessyban.webgl2

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.gessyban.GameResources
import net.perfectdreams.gessyban.Gessyban
import net.perfectdreams.gessyban.Stage
import net.perfectdreams.gessyban.VirtualFileSystem
import net.perfectdreams.gessyban.meshes.JsonMeshManager
import net.perfectdreams.gessyban.webgl2.textures.WebTextureManager
import net.perfectdreams.harmony.gl.GPUResources
import net.perfectdreams.harmony.gl.webgl2.WebGL2Context
import web.animations.requestAnimationFrame
import web.dom.document
import web.events.addHandler
import web.gl.WebGL2RenderingContext
import web.gl.WebGLContextAttributes
import web.gl.ID
import web.html.HTMLCanvasElement
import web.html.Image
import web.keyboard.KeyA
import web.keyboard.KeyCode
import web.keyboard.KeyD
import web.keyboard.KeyS
import web.keyboard.KeyW
import web.mouse.MouseEvent
import web.dom.mouseDownEvent
import web.dom.mouseMoveEvent
import web.html.wheelEvent
import web.window.keyUpEvent
import web.window.window

class GessybanWebGL2(val vfs: VirtualFileSystem) {
    lateinit var gessyban: Gessyban

    fun start() {
        // Get the Canvas from the DOM
        val canvas = document.querySelector("#glCanvas") as HTMLCanvasElement

        // We disable the alpha channel because the game renders things with alpha < 1.0 to the screen, which would cause the page to bleed through the canvas
        val webgl2 = canvas.getContext(WebGL2RenderingContext.ID, WebGLContextAttributes(alpha = false)) ?: error("WebGL2 is not supported!")

        window.keyUpEvent.addHandler {
            when (val stage = gessyban.stage) {
                is Stage.MainMenu -> {}
                is Stage.Game -> {
                    when (it.code) {
                        KeyCode.KeyD -> stage.game.movePlayer(1, 0)
                        KeyCode.KeyA -> stage.game.movePlayer(-1, 0)
                        KeyCode.KeyW -> stage.game.movePlayer(0, 1)
                        KeyCode.KeyS -> stage.game.movePlayer(0, -1)
                    }
                }
            }
        }

        canvas.wheelEvent.addHandler {
            if (0 > it.deltaY) {
                gessyban.zoom -= 1f
            } else if (it.deltaY > 0) {
                gessyban.zoom += 1f
            }

            gessyban.zoom = gessyban.zoom.coerceIn(2f..8f)

            it.preventDefault()
        }

        canvas.mouseDownEvent.addHandler {
            val (x, y) = getCursorPositionOnCanvas(canvas, it)
            gessyban.stage.onClick(x, y)
        }

        canvas.mouseMoveEvent.addHandler {
            val (x, y) = getCursorPositionOnCanvas(canvas, it)
            gessyban.stage.onCursorMove(x, y)
        }

        val gl = WebGL2Context(webgl2)
        val gpu = GPUResources(gl, webgl2.drawingBufferWidth, webgl2.drawingBufferHeight)
        val textureManager = WebTextureManager(gpu, vfs)

        GlobalScope.launch {
            val resources = GameResources.load(gpu, textureManager, JsonMeshManager(),  vfs)
            this@GessybanWebGL2.gessyban = Gessyban(gl, gpu, vfs, resources)

            // Set the clear color
            gl.clearColor(23 / 255f, 35 / 255f, 35 / 255f, 1.0f)

            fun render() {
                gessyban.render()

                requestAnimationFrame { render() }
            }

            requestAnimationFrame { render() }
        }
    }

    /**
     * Gets the cursor position in canvas pixels, because the canvas may be scaled by CSS
     */
    private fun getCursorPositionOnCanvas(canvas: HTMLCanvasElement, event: MouseEvent): Pair<Double, Double> {
        // https://stackoverflow.com/a/42111623
        val rect = canvas.getBoundingClientRect()
        val x = (event.clientX - rect.left) * (canvas.width / rect.width)
        val y = (event.clientY - rect.top) * (canvas.height / rect.height)
        return Pair(x, y)
    }
}
