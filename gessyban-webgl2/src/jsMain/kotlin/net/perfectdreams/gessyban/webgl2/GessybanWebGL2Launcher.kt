package net.perfectdreams.gessyban.webgl2

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import js.function.JsFunction
import js.promise.Promise
import js.promise.await
import js.typedarrays.Uint8Array
import js.typedarrays.toByteArray
import js.typedarrays.toUint8Array
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.gessyban.VirtualFileSystem
import web.blob.Blob
import web.events.EventHandler
import web.html.Image
import web.url.URL
import web.window.window

fun main() {
    println("Attempting to load game assets...")
    val http = HttpClient {}

    GlobalScope.launch {
        val currentFolder = window.location.href.substring(0, window.location.href.lastIndexOf("/"))

        val response = http.get("$currentFolder/assets.zip")

        val jszip = JSZip()

        jszip.loadAsync(response.bodyAsBytes().toUint8Array()).await()
        val jsZipObjects = mutableMapOf<String, JSZipObject>()

        jszip.forEach { relativePath, file ->
            if (!file.dir)
                jsZipObjects[relativePath] = file
        }

        val vfs = VirtualFileSystem()

        // Now we FINALLY attempt to actually read the damn things
        // We do it like this because "async" is, guess what, async and it uses a promise
        // And don't get fooled, the "jszip.forEach" function is NOT Kotlin's forEach, which is why we can't do await there
        for ((relativePath, file) in jsZipObjects) {
            val uint8array = file.async("uint8array").await() as Uint8Array<*>
            var byteArray = uint8array.toByteArray()

            // Our shaders are written for desktop OpenGL, so we need to convert them to GLSL ES
            if (relativePath.endsWith(".vsh") || relativePath.endsWith(".fsh"))
                byteArray = convertShaderToGLSLES(byteArray.decodeToString()).encodeToByteArray()

            vfs.files[relativePath] = byteArray
        }

        val m = GessybanWebGL2(vfs)
        m.start()
    }
}

/**
 * Converts a desktop GLSL shader to a GLSL ES 3.00 shader, by replacing the `#version` directive and adding the required precision qualifiers
 */
private fun convertShaderToGLSLES(source: String): String {
    val lines = source.lines()
    require(lines.first().startsWith("#version")) { "Shader does not start with a #version directive!" }

    return (
        listOf(
            "#version 300 es",
            "precision highp float;",
            "precision highp int;",
            // The default precision of sampler2D is lowp, which is not enough for depth textures
            "precision highp sampler2D;"
        ) + lines.drop(1)
    ).joinToString("\n")
}