import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import java.net.URI

plugins {
    id("dev.architectury.loom")
    kotlin("jvm")
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "2.+"
    id("com.github.johnrengelman.shadow")
}

evaluationDependsOn(":common")

architectury {
    platformSetupLoomIde()
    neoForge()
}

val archivesBaseName: String by rootProject

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating

configurations {
    common
    shadowCommon // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath { extendsFrom(common) }
    runtimeClasspath { extendsFrom(common) }
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath

    mods.getByName("main") {
        sourceSet(sourceSets.main.get())
        sourceSet(project(":common").sourceSets.main.get())
    }
}

repositories {
    maven {
        url = URI("https://maven.neoforged.net/releases")
    }
    maven {
        url = URI("https://thedarkcolour.github.io/KotlinForForge/")
    }
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
    modRuntimeOnly("me.fzzyhmstrs:fzzy_config:$fzzyConfigVersion+neoforge") {
        exclude("net.fabricmc.fabric-api")
    }

    // Neo only deps

    val neoforgeLoaderVersion: String by rootProject
    neoForge("net.neoforged:neoforge:$neoforgeLoaderVersion")
    val kotlinForForgeVersion: String by project
    modImplementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForForgeVersion")

    "common"(project(":common", "namedElements")) { this.setTransitive(false) }
    "shadowCommon"(project(":common", "transformProductionNeoForge")) { this.setTransitive(false) }
}

val modName: String by rootProject
val modDesc: String by rootProject
val modAuthor: String by rootProject
val modAuthorSite: String by rootProject
val modYear: String by rootProject
val modSources: String by rootProject
val modIssues: String by rootProject
val modHomepage: String by rootProject

tasks {

    processResources {
        val neoforgeLoaderVersion: String by rootProject
        val kotlinForForgeVersion: String by rootProject
        val fzzyConfigVersion: String by rootProject
        inputs.property("version", project.version)
        inputs.property("id", base.archivesName.get())
        inputs.property("neoforgeLoaderVersion", neoforgeLoaderVersion)
        inputs.property("kotlinForForgeVersion", kotlinForForgeVersion)
        inputs.property("fzzyConfigVersion", fzzyConfigVersion)
        inputs.property("modName", modName)
        inputs.property("modDesc", modDesc)
        inputs.property("modAuthor", modAuthor)
        inputs.property("modSources", modSources)
        inputs.property("modIssues", modIssues)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mutableMapOf(
                "version" to project.version,
                "id" to base.archivesName.get(),
                "neoforgeLoaderVersion" to neoforgeLoaderVersion,
                "kotlinForForgeVersion" to kotlinForForgeVersion,
                "fzzyConfigVersion" to fzzyConfigVersion,
                "modName" to modName,
                "modDesc" to modDesc,
                "modAuthor" to modAuthor,
                "modSources" to modSources,
                "modIssues" to modIssues)
            )
        }
    }

    shadowJar {
        exclude("architectury.common.json")

        configurations = mutableListOf<FileCollection>(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener = true
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
        archiveClassifier.set("")
    }

    sourcesJar {
        val commonSources = project(":common").tasks.sourcesJar
        dependsOn(commonSources)
        from(commonSources.get().archiveFile.map { zipTree(it) })
    }

    modrinth.get().group = "upload"
    modrinthSyncBody.get().group = "upload"
}

with(components["java"] as AdhocComponentWithVariants) {
    withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }
}

val log: File = File(project.rootDir, "changelog.md")

if (System.getenv("MODRINTH_TOKEN") != null) {
    modrinth {
        val modrinthSlugName: String by rootProject
        val mcVersions: String by rootProject
        val modLoaders: String by project
        val releaseType: String by rootProject
        val uploadDebugMode: String by rootProject

        val realProjectId = if(modrinthSlugName.isEmpty()) {
            archivesBaseName.replace('_', '-')
        } else {
            modrinthSlugName
        }

        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set(realProjectId)
        versionNumber.set("${project.version}")
        versionName.set("${base.archivesName.get()}-${project.version}")
        versionType.set(releaseType)
        uploadFile.set(tasks.remapJar.get())
        gameVersions.addAll(mcVersions.split(","))
        loaders.addAll(modLoaders.split(",").map { it.lowercase() })
        detectLoaders.set(false)
        changelog.set("## Changelog for " + "${project.version} \n\n" + log.readText())
        dependencies {
            val requiredDeps: String by project
            for (dep in requiredDeps.split(",")) {
                if (dep.isEmpty()) continue
                required.project(dep)
            }
            val optionalDeps: String by project
            for (dep in optionalDeps.split(",")) {
                if (dep.isEmpty()) continue
                optional.project(dep)
            }
        }
        debugMode.set(uploadDebugMode.toBooleanLenient() ?: true)
    }
}

val curseProjectId: String by rootProject
if (System.getenv("CURSEFORGE_TOKEN") != null && curseProjectId.isNotEmpty()) {
    curseforge {
        val mcCurseVersions: String by rootProject
        val mcVersions: String by rootProject
        val modLoaders: String by project
        val releaseType: String by rootProject
        val uploadDebugMode: String by rootProject


        val realVersions = if(mcCurseVersions.isEmpty()) {
            mcVersions.split(",")
        } else {
            mcCurseVersions.split(",")
        }

        apiKey = System.getenv("CURSEFORGE_TOKEN")
        project(closureOf<CurseProject> {
            id = curseProjectId
            changelog = log
            changelogType = "markdown"
            this.releaseType = releaseType
            for (ver in realVersions) {
                addGameVersion(ver)
            }
            for (ml in modLoaders.split(",")) {
                addGameVersion(ml)
            }
            mainArtifact(tasks.remapJar.get().archiveFile.get(), closureOf<CurseArtifact> {
                displayName = "${base.archivesName.get()}-${project.version}"
                relations(closureOf<CurseRelation> {
                    val requiredDeps: String by project
                    for (dep in requiredDeps.split(",")) {
                        if (dep.isEmpty()) continue
                        this.requiredDependency(dep)
                    }
                    val optionalDeps: String by project
                    for (dep in optionalDeps.split(",")) {
                        if (dep.isEmpty()) continue
                        this.optionalDependency(dep)
                    }
                })
            })
            relations(closureOf<CurseRelation> {
                val requiredDeps: String by project
                for (dep in requiredDeps.split(",")) {
                    if (dep.isEmpty()) continue
                    this.requiredDependency(dep)
                }
                val optionalDeps: String by project
                if (optionalDeps.isNotEmpty()) {
                    for (dep in optionalDeps.split(",")) {
                        if (dep.isEmpty()) continue
                        this.optionalDependency(dep)
                    }
                }
            })
        })
        options(closureOf<Options> {
            javaIntegration = false
            forgeGradleIntegration = false
            javaVersionAutoDetect = false
            debug = uploadDebugMode.toBooleanLenient() ?: true
        })
    }
}

tasks.register("uploadAll") {
    group = "upload"
    dependsOn(tasks.modrinth.get())
    dependsOn(tasks.curseforge.get())
}



publishing {
    publications {
        create<MavenPublication>(archivesBaseName) {
            from(components["java"])

            pom {
                name.set(modName)
                description.set(modDesc)
                inceptionYear.set(modYear)
                licenses {
                    license {
                        name.set("TDL-M")
                        url.set("https://github.com/fzzyhmstrs/Timefall-Development-Licence-Modified")
                        distribution.set("repo")
                        comments.set("$modName is free software provided under the terms of the Timefall Development License - Modified (TDL-M). See license url for full license details.")
                    }
                }
                if (modSources.isNotEmpty()) {
                    scm {
                        url.set(modSources)
                    }
                }
                if (modIssues.isNotEmpty()) {
                    issueManagement {
                        system.set("Github")
                        url.set(modIssues)
                    }
                }
                developers {
                    developer {
                        name.set(modAuthor)
                        if (modAuthorSite.isNotEmpty()) {
                            url.set(modAuthorSite)
                        }
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "FzzyMaven"
            url = uri("https://maven.fzzyhmstrs.me")
            credentials {
                username = System.getProperty("fzzyMavenUsername")
                password = System.getProperty("fzzyMavenPassword")
            }
        }
    }
}