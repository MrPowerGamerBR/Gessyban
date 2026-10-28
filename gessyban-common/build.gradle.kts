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
                implementation("net.perfectdreams.harmony:harmony-gl:0.0.1-SNAPSHOT")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
            }
        }
    }
}