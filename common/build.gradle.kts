plugins {
    id("dev.architectury.loom")
    kotlin("jvm")
}

val minecraftVersion: String by project
val modVersion: String by project
val enabledPlatforms: String by rootProject

architectury {
    common(enabledPlatforms.split(","))
}

dependencies {

    minecraft("com.mojang:minecraft:$minecraftVersion")
    val yarnMappings: String by project
    val yarnMappingsPatchVersion: String by project
    mappings( loom.layered {
        mappings("net.fabricmc:yarn:$yarnMappings:v2")
        mappings("dev.architectury:yarn-mappings-patch-neoforge:$yarnMappingsPatchVersion")
    })

    val fzzyConfigVersion: String by rootProject
    modCompileOnly("me.fzzyhmstrs:fzzy_config:$fzzyConfigVersion") {
        exclude("net.fabricmc.fabric-api")
    }
}

val archivesBaseName: String by rootProject

if (File("src/main/resources/${archivesBaseName}.accesswidener").exists()) {
    loom {
        accessWidenerPath = file("src/main/resources/${archivesBaseName}.accesswidener")
    }
}

tasks {
    jar {
        from(rootProject.file( "LICENSE")) { rename { "${base.archivesName.get()}_${it}" } }
        from( rootProject.file("credits.txt")) { rename { "${base.archivesName.get()}_${it}" } }
        from( rootProject.file("changelog.md")) { rename { "${modVersion}+${minecraftVersion}_${it}" } }
        filesMatching("template_multiloader.mixins.json") {
            rename { n ->  println(n); n.replace("template_multiloader", archivesBaseName) }
        }
        exclude("package-info.java")
    }

    processResources {
        inputs.property("id", base.archivesName.get())
        filesMatching("*.mixins.json") {
            expand(mutableMapOf(
                "id" to base.archivesName.get()
            ))
        }
    }
}