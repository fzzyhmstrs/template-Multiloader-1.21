plugins {
    id("dev.architectury.loom")
    kotlin("jvm")
}

val minecraftVersion: String by rootProject

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
        from("LICENSE") { rename { "${base.archivesName.get()}_${it}" } }
    }
    jar {
        from( "credits.txt") { rename { "${base.archivesName.get()}_${it}" } }
    }

    processResources {
        inputs.property("id", base.archivesName.get())
        filesMatching("*.mixins.json") {
            rename { n -> n.replace("template_multiloader", archivesBaseName) }
            expand(mutableMapOf(
                "id" to base.archivesName.get()
            ))
        }
    }
}