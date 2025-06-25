package net.perfectdreams.gessyban

import kotlinx.serialization.json.Json
import net.perfectdreams.gessyban.meshes.JsonMeshLoader
import net.perfectdreams.gessyban.shaders.Default2DShader
import net.perfectdreams.gessyban.shaders.Default3DShader
import net.perfectdreams.gessyban.shaders.PixelFadeShader
import net.perfectdreams.gessyban.shaders.PixelFilterShader
import net.perfectdreams.gessyban.skeletons.AnimationBone
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11.GL_LINEAR
import org.lwjgl.opengl.GL11.GL_NEAREST
import java.io.File
import kotlin.collections.Map

class GameResources(val m: Gessyban) {
    val screenProjectionMatrix = Matrix4f()
        .ortho(0.0f, 1280f, 720f, 0.0f, -1.0f, 1.0f)

    val pixelProjectionMatrix = Matrix4f()
        .ortho(0.0f, 320f, 180f, 0.0f, -1.0f, 1.0f)

    val shader2d = m.shaderManager.loadShader(
        File("assets/shaders/game_2d.vsh").readText(),
        File("assets/shaders/game_2d.fsh").readText(),
        ::Default2DShader
    )

    val shader3d = m.shaderManager.loadShader(
        File("assets/shaders/game_3d.vsh").readText(),
        File("assets/shaders/game_3d.fsh").readText(),
        ::Default3DShader
    )

    val shaderPixelFilter = m.shaderManager.loadShader(
        File("assets/shaders/game_2d.vsh").readText(),
        File("assets/shaders/pixel_filter.fsh").readText(),
        ::PixelFilterShader
    )

    val shaderPixelFade = m.shaderManager.loadShader(
        File("assets/shaders/game_2d.vsh").readText(),
        File("assets/shaders/pixel_fade.fsh").readText(),
        ::PixelFadeShader
    )

    val furalhaTexture = m.textureManager.loadTexture("assets/textures/furalha.png", GL_NEAREST)
    val wallTexture = m.textureManager.loadTexture("assets/textures/wall.png", GL_NEAREST)
    val pressurePlateTexture = m.textureManager.loadTexture("assets/textures/pressure_plate.png", GL_NEAREST)
    val boxTexture = m.textureManager.loadTexture("assets/textures/box.png", GL_NEAREST)
    val floorTexture = m.textureManager.loadTexture("assets/textures/floor.png", GL_NEAREST)
    val gessyTexture = m.textureManager.loadTexture("assets/textures/gessy_texture.png", GL_NEAREST)
    val logoTexture = m.textureManager.loadTexture("assets/textures/logo.png", GL_NEAREST)
    val startButtonTexture = m.textureManager.loadTexture("assets/textures/start_button.png", GL_NEAREST)
    val startButtonHoverTexture = m.textureManager.loadTexture("assets/textures/start_button_hover.png", GL_NEAREST)
    val fontTexture = m.textureManager.loadTexture("assets/textures/font.png", GL_NEAREST)

    val wallMesh = m.meshManager.createJsonMeshVAO(JsonMeshLoader.load(File("assets/meshes/wall.json").readText()))
    val floorMesh = m.meshManager.createJsonMeshVAO(JsonMeshLoader.load(File("assets/meshes/floor.json").readText()))
    // val gessyMesh = meshManager.createJsonMeshVAO(JsonMeshLoader.load(File("assets/meshes/gessy.json").readText()))

    val gessySkeleton = m.meshManager.createJsonMeshSkeleton(
        JsonMeshLoader.load(File("assets/meshes/gessy.json").readText()),
        File("assets/armatures/gessy_skeleton.json").readText()
    )

    val gessyIdlePose = Json.decodeFromString<Map<String, AnimationBone>>(File("assets/poses/idle.json").readText())
    val gessyWalkPose = Json.decodeFromString<Map<String, AnimationBone>>(File("assets/poses/walk.json").readText())
    val gessyHiPose = Json.decodeFromString<Map<String, AnimationBone>>(File("assets/poses/hi.json").readText())

    val screenQuadMesh = m.meshManager.createMesh2D(JsonMeshLoader.load(File("assets/meshes/plane_2d.json").readText()))
    // val screenQuadMesh = m.setupScreenQuad()
    val fontCharactersMeshes = m.meshManager.createFontAtlas()

    val aspectRatio = 1280f / 720f

    val menuFramebuffer = m.framebufferManager.createFramebuffer(1280, 720)
    val guiFramebuffer = m.framebufferManager.createFramebuffer(1280, 720)
    val gameFramebuffer = m.framebufferManager.createFramebuffer(320, 180)
    val objectIdFramebuffer = m.framebufferManager.createFramebuffer(320, 180)
    val normalsFramebuffer = m.framebufferManager.createFramebuffer(320, 180)
    val pixelFilterFramebuffer = m.framebufferManager.createFramebuffer(320, 180)
    val upscaledFramebuffer = m.framebufferManager.createFramebuffer(1280, 720)
}