package net.perfectdreams.gessyban

import com.mrpowergamerbr.starfait.Matrix4f
import kotlinx.serialization.json.Json
import net.perfectdreams.gessyban.meshes.JsonMeshLoader
import net.perfectdreams.gessyban.shaders.Default2DShader
import net.perfectdreams.gessyban.shaders.Default3DShader
import net.perfectdreams.gessyban.shaders.PixelFadeShader
import net.perfectdreams.gessyban.shaders.PixelFilterShader
import net.perfectdreams.gessyban.skeletons.AnimationBone
import net.perfectdreams.harmony.gl.GLConstants.GL_CLAMP_TO_EDGE
import net.perfectdreams.harmony.gl.GLConstants.GL_NEAREST
import net.perfectdreams.harmony.graphics.batcher.SpriteBatcher
import kotlin.collections.Map

class GameResources(val m: Gessyban, val vfs: VirtualFileSystem) {
    val shader2d = m.gpu.createShader(
        vfs.files["shaders/game_2d.vsh"]!!.decodeToString(),
        vfs.files["shaders/game_2d.fsh"]!!.decodeToString(),
        ::Default2DShader
    )

    val spriteBatcher = SpriteBatcher(m.gl)

    val screenProjectionMatrix = Matrix4f()
        .ortho(0.0f, 1280f, 720f, 0.0f, -1.0f, 1.0f)

    val pixelProjectionMatrix = Matrix4f()
        .ortho(0.0f, 320f, 180f, 0.0f, -1.0f, 1.0f)

    val shader3d = m.gpu.createShader(
        vfs.files["shaders/game_3d.vsh"]!!.decodeToString(),
        vfs.files["shaders/game_3d.fsh"]!!.decodeToString(),
        ::Default3DShader
    )

    val shaderPixelFilter =  m.gpu.createShader(
        vfs.files["shaders/pixel_fade.vsh"]!!.decodeToString(),
        vfs.files["shaders/pixel_filter.fsh"]!!.decodeToString(),
        ::PixelFilterShader
    )

    val shaderPixelFade = m.gpu.createShader(
        vfs.files["shaders/pixel_fade.vsh"]!!.decodeToString(),
        vfs.files["shaders/pixel_fade.fsh"]!!.decodeToString(),
        ::PixelFadeShader
    )

    val furalhaTexture = m.textureManager.loadTexture("textures/furalha.png", GL_NEAREST)
    val wallTexture = m.textureManager.loadTexture("textures/wall.png", GL_NEAREST)
    val pressurePlateTexture = m.textureManager.loadTexture("textures/pressure_plate.png", GL_NEAREST)
    val boxTexture = m.textureManager.loadTexture("textures/box.png", GL_NEAREST)
    val floorTexture = m.textureManager.loadTexture("textures/floor.png", GL_NEAREST)
    val gessyTexture = m.textureManager.loadTexture("textures/gessy_texture.png", GL_NEAREST)
    val logoTexture = m.textureManager.loadTexture("textures/logo.png", GL_NEAREST)
    val startButtonTexture = m.textureManager.loadTexture("textures/start_button.png", GL_NEAREST)
    val startButtonHoverTexture = m.textureManager.loadTexture("textures/start_button_hover.png", GL_NEAREST)
    val fontTexture = m.textureManager.loadTexture("textures/font.png", GL_NEAREST)

    val wallMesh = m.meshManager.createJsonMeshVAO(m.gl, JsonMeshLoader.load(vfs.files["meshes/wall.json"]!!.decodeToString()))
    val floorMesh = m.meshManager.createJsonMeshVAO(m.gl, JsonMeshLoader.load(vfs.files["meshes/floor.json"]!!.decodeToString()))
    // val gessyMesh = meshManager.createJsonMeshVAO(JsonMeshLoader.load(File("meshes/gessy.json").readText()))

    val gessySkeleton = m.meshManager.createJsonMeshSkeleton(
        m.gl,
        JsonMeshLoader.load(vfs.files["meshes/gessy.json"]!!.decodeToString()),
        vfs.files["armatures/gessy_skeleton.json"]!!.decodeToString()
    )

    val gessyIdlePose = Json.decodeFromString<Map<String, AnimationBone>>(vfs.files["poses/idle.json"]!!.decodeToString())
    val gessyWalkPose = Json.decodeFromString<Map<String, AnimationBone>>(vfs.files["poses/walk.json"]!!.decodeToString())
    val gessyHiPose = Json.decodeFromString<Map<String, AnimationBone>>(vfs.files["poses/hi.json"]!!.decodeToString())

    val screenQuadMesh = m.meshManager.createMesh2D(m.gl, JsonMeshLoader.load(vfs.files["meshes/plane_2d.json"]!!.decodeToString()))
    // val screenQuadMesh = m.setupScreenQuad()
    val fontCharactersMeshes = m.meshManager.createFontAtlas()

    val aspectRatio = 1280f / 720f

    val menuFramebuffer = m.gpu.createFramebuffer(1280, 720, true, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val guiFramebuffer = m.gpu.createFramebuffer(1280, 720, false, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val gameFramebuffer = m.gpu.createFramebuffer(320, 180, true, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val objectIdFramebuffer = m.gpu.createFramebuffer(320, 180, true, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val normalsFramebuffer = m.gpu.createFramebuffer(320, 180, true, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val pixelFilterFramebuffer = m.gpu.createFramebuffer(320, 180, false, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val upscaledFramebuffer = m.gpu.createFramebuffer(1280, 720, false, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
}