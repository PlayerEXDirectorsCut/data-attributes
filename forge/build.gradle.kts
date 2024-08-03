import java.util.Objects

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    forge()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentForge").extendsFrom(configurations["common"])
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

        mixinConfig("data_attributes-common.mixins.json")
        mixinConfig("data_attributes.mixins.json")
    }

    // Forge Datagen Gradle config.  Remove if not using Forge datagen
    runs.create("datagen") {
        data()
        programArgs("--all", "--mod", "data_attributes")
        programArgs("--output", project(":common").file("src/main/generated/resources").absolutePath)
        programArgs("--existing", project(":common").file("src/main/resources").absolutePath)
    }
}

dependencies {
    forge("net.minecraftforge:forge:$minecraftVersion-${project.properties["forge_version"]}")
    implementation("thedarkcolour:kotlinforforge:${project.properties["kotlin_forge_version"]}")

    modImplementation("dev.su5ed.sinytra.fabric-api:fabric-api:${project.properties["forgified_fabric_api_version"]}")

    modImplementation("io.wispforest:endec:${project.properties["endec_version"]}")!!.let(::include)
    modImplementation("io.wispforest.endec:netty:${project.properties["endec_netty_version"]}")!!.let(::include)

    compileOnly("io.github.llamalad7:mixinextras-common:${project.properties["mixinextras_version"]}")!!.let(::annotationProcessor)
    implementation("io.github.llamalad7:mixinextras-forge:${project.properties["mixinextras_version"]}")!!.let(::include)

    minecraftLibraries("org.sinytra:Connector:${project.properties["connector_version"]}")
    runtimeOnly("maven.modrinth:forgified-fabric-api:${project.properties["forgified_fabric_api_version"]}")

//    classpath('org.spongepowered:mixingradle:0.7-SNAPSHOT')

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionForge")) { isTransitive = false }
}

tasks {
    base.archivesName.set(base.archivesName.get() + "-Forge")
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("fabric.mod.json")
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar.get().archiveClassifier.set("dev")

    sourcesJar {
        val commonSources = project(":common").tasks.sourcesJar
        dependsOn(commonSources)
        from(commonSources.get().archiveFile.map { zipTree(it) })
    }
}

// afterEvaluate {
//     val cleanArtifactJar =  requireNotNull(net.minecraftforge.gradle.common.util.MavenArtifactDownloader.generate(project, "net.minecraft:joined:${project.properties["mcp_version"]}:srg", true))
//     minecraft.runs.configureEach {
//         property("connector.clean.path", cleanArtifactJar);
//     }
// }

components {
    java.run {
        if (this is AdhocComponentWithVariants)
            withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) { skip() }
    }
}

publishing {
    publications.create<MavenPublication>("mavenForge") {
        artifactId = "${project.properties["archives_base_name"]}" + "-Forge"
        from(components["java"])
    }

    repositories {
        mavenLocal()
        maven {
            val releasesRepoUrl = "https://example.com/releases"
            val snapshotsRepoUrl = "https://example.com/snapshots"
            url = uri(if (project.version.toString().endsWith("SNAPSHOT") || project.version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
            name = "ExampleRepo"
            credentials {
                username = project.properties["repoLogin"]?.toString()
                password = project.properties["repoPassword"]?.toString()
            }
        }
    }
}