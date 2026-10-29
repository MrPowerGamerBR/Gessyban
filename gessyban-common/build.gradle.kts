plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

group = "net.perfectdreams.gessyban"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)

    jvm()
    js {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.harmony.gl)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
            }
        }
    }
}