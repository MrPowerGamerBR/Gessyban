plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Gessyban"

include(":gessyban-common")
include(":gessyban-lwjgl")
include(":gessyban-webgl2")