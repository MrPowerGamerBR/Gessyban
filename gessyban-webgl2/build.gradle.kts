plugins {
    kotlin("multiplatform")
}

group = "net.perfectdreams.gessyban"
version = "1.0-SNAPSHOT"

kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain {
            dependencies {
                implementation(project(":gessyban-common"))
                implementation(libs.harmony.gl)
                implementation(libs.harmony.webgl2)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
                // Required for WebGL2 Bindings
                implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.10.5")
                implementation(npm("jszip", "3.10.1"))
                implementation("io.ktor:ktor-client-js:3.0.3")
            }
        }
    }
}

// Packs the root "assets" folder into an "assets.zip" file, which is downloaded by the game when it is loaded
val assetsZip = tasks.register<Zip>("assetsZip") {
    from(rootProject.file("assets"))
    archiveFileName = "assets.zip"
    destinationDirectory = layout.buildDirectory.dir("assetsZip")
}

tasks.named<ProcessResources>("jsProcessResources") {
    from(assetsZip)
}
