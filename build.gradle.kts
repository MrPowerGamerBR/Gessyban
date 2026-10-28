plugins {
    kotlin("multiplatform") version "2.4.21" apply false
    kotlin("plugin.serialization") version "2.4.21" apply false
}

group = "net.perfectdreams.gessyban"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.perfectdreams.net/")
    }
}