package net.perfectdreams.gessyban

import com.mrpowergamerbr.starfait.Matrix4f
import kotlinx.serialization.json.Json
import net.perfectdreams.gessyban.meshes.JsonMeshLoader
import net.perfectdreams.gessyban.meshes.JsonMeshManager
import net.perfectdreams.gessyban.shaders.Default2DShader
import net.perfectdreams.gessyban.shaders.Default3DShader
import net.perfectdreams.gessyban.shaders.PixelFadeShader
import net.perfectdreams.gessyban.shaders.PixelFilterShader
import net.perfectdreams.gessyban.skeletons.AnimationBone
import net.perfectdreams.gessyban.skeletons.Skeleton
import net.perfectdreams.harmony.gl.GLConstants
import net.perfectdreams.harmony.gl.GLConstants.GL_CLAMP_TO_EDGE
import net.perfectdreams.harmony.gl.GLConstants.GL_NEAREST
import net.perfectdreams.harmony.render.RenderDevice
import net.perfectdreams.harmony.render.textures.Texture
import net.perfectdreams.harmony.render.batcher.SpriteBatcher
import net.perfectdreams.harmony.render.textures.TextureFormat
import net.perfectdreams.oldharmony.gl.meshes.MeshRenderingState
import kotlin.collections.Map

class GameResources(
    val device: RenderDevice,
    val vfs: VirtualFileSystem,
    val shader2d: Default2DShader,
    val shader3d: Default3DShader,
    val shaderPixelFilter: PixelFilterShader,
    val shaderPixelFade: PixelFadeShader,
    val furalhaTexture: Texture,
    val wallTexture: Texture,
    val pressurePlateTexture: Texture,
    val boxTexture: Texture,
    val floorTexture: Texture,
    val gessyTexture: Texture,
    val logoTexture: Texture,
    val startButtonTexture: Texture,
    val startButtonHoverTexture: Texture,
    val fontTexture: Texture,
    val wallMesh: MeshRenderingState,
    val floorMesh: MeshRenderingState,
    val gessySkeleton: Skeleton,
    val gessyIdlePose: Map<String, AnimationBone>,
    val gessyWalkPose: Map<String, AnimationBone>,
    val gessyHiPose: Map<String, AnimationBone>,
    val screenQuadMesh: MeshRenderingState,
    val fontCharactersMeshes: Map<Char, FontCharacter>,
) {
    companion object {
        suspend fun load(
            render: RenderDevice,
            textureManager: TextureManager,
            meshManager: JsonMeshManager,
            vfs: VirtualFileSystem
        ): GameResources {
            val gl = render.gl
            return GameResources(
                render,
                vfs,
                render.createShader(
                    vfs.files["shaders/game_2d.vsh"]!!.decodeToString(),
                    vfs.files["shaders/game_2d.fsh"]!!.decodeToString(),
                    ::Default2DShader
                ),
                render.createShader(
                    vfs.files["shaders/game_3d.vsh"]!!.decodeToString(),
                    vfs.files["shaders/game_3d.fsh"]!!.decodeToString(),
                    ::Default3DShader
                ),
                render.createShader(
                    vfs.files["shaders/pixel_fade.vsh"]!!.decodeToString(),
                    vfs.files["shaders/pixel_filter.fsh"]!!.decodeToString(),
                    ::PixelFilterShader
                ),
                render.createShader(
                    vfs.files["shaders/pixel_fade.vsh"]!!.decodeToString(),
                    vfs.files["shaders/pixel_fade.fsh"]!!.decodeToString(),
                    ::PixelFadeShader
                ),
                textureManager.loadTexture("textures/furalha.png", GL_NEAREST),
                textureManager.loadTexture("textures/wall.png", GL_NEAREST),
                textureManager.loadTexture("textures/pressure_plate.png", GL_NEAREST),
                textureManager.loadTexture("textures/box.png", GL_NEAREST),
                textureManager.loadTexture("textures/floor.png", GL_NEAREST),
                textureManager.loadTexture("textures/gessy_texture.png", GL_NEAREST),
                textureManager.loadTexture("textures/logo.png", GL_NEAREST),
                textureManager.loadTexture("textures/start_button.png", GL_NEAREST),
                textureManager.loadTexture("textures/start_button_hover.png", GL_NEAREST),
                textureManager.loadTexture("textures/font.png", GL_NEAREST),
                meshManager.createJsonMeshVAO(gl, JsonMeshLoader.load(vfs.files["meshes/wall.json"]!!.decodeToString())),
                meshManager.createJsonMeshVAO(gl, JsonMeshLoader.load(vfs.files["meshes/floor.json"]!!.decodeToString())),
                meshManager.createJsonMeshSkeleton(
                    gl,
                    JsonMeshLoader.load(vfs.files["meshes/gessy.json"]!!.decodeToString()),
                    vfs.files["armatures/gessy_skeleton.json"]!!.decodeToString()
                ),
                Json.decodeFromString<Map<String, AnimationBone>>(vfs.files["poses/idle.json"]!!.decodeToString()),
                Json.decodeFromString<Map<String, AnimationBone>>(vfs.files["poses/walk.json"]!!.decodeToString()),
                Json.decodeFromString<Map<String, AnimationBone>>(vfs.files["poses/hi.json"]!!.decodeToString()),
                meshManager.createMesh2D(gl, JsonMeshLoader.load(vfs.files["meshes/plane_2d.json"]!!.decodeToString())),
                meshManager.createFontAtlas(),

            )
        }
    }

    val spriteBatcher = SpriteBatcher(device.gl)
    val screenProjectionMatrix = Matrix4f().ortho(0.0f, 1280f, 720f, 0.0f, -1.0f, 1.0f)
    val pixelProjectionMatrix = Matrix4f().ortho(0.0f, 320f, 180f, 0.0f, -1.0f, 1.0f)

    val aspectRatio = 1280f / 720f

    val menuFramebuffer = device.createFramebuffer(1280, 720, TextureFormat.DEPTH24, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val guiFramebuffer = device.createFramebuffer(1280, 720, null, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val gameFramebuffer = device.createFramebuffer(320, 180, TextureFormat.DEPTH24, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val objectIdFramebuffer = device.createFramebuffer(320, 180, TextureFormat.DEPTH24, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val normalsFramebuffer = device.createFramebuffer(320, 180, TextureFormat.DEPTH24, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val pixelFilterFramebuffer = device.createFramebuffer(320, 180, null, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
    val upscaledFramebuffer = device.createFramebuffer(1280, 720, null, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, GL_NEAREST, GL_NEAREST)
}