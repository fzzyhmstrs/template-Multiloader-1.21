pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        maven("https://maven.architectury.dev/") { name = "Arch" }
        maven("https://maven.minecraftforge.net/") { name = "Forge" }
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        val loomVersion: String by settings
        id("dev.architectury.loom").version(loomVersion).apply(false)
        val pluginVersion: String by settings
        id("architectury-plugin").version(pluginVersion).apply(false)
        val shadowVersion: String by settings
        id("com.github.johnrengelman.shadow").version(shadowVersion).apply(false)
    }
}

val archivesBaseName: String by settings

rootProject.name = archivesBaseName

include("common")
include("fabric")
include("neoforge")