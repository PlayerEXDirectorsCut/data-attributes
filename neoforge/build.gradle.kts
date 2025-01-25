plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentNeoForge").extendsFrom(configurations["common"])
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

dependencies {
    neoForge("net.neoforged:neoforge:${project.properties["neoforge_version"]}")
    implementation("thedarkcolour:kotlinforforge-neoforge:${project.properties["kotlin_forge_version"]}") {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }

    implementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:${properties["fabric_api_version"]}+${properties["ffapi_version"]}+${minecraftVersion}") {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }

    modImplementation("maven.modrinth:owo-lib:${properties["owo_neo_version"]}") {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }
//    include("io.wispforest:owo-sentinel:${properties["owo_neo_version"]}")

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionNeoForge")) { isTransitive = false }
}

tasks {
    base.archivesName.set(base.archivesName.get() + "-neoforge")
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
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
        from(commonSources.get().archiveFile.map(::zipTree))
    }
}

components {
    java.run {
        if (this is AdhocComponentWithVariants)
            withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) { skip() }
    }
}

publishing {
    publications.create<MavenPublication>("mavenNeoForge") {
        artifactId = "${project.properties["archives_base_name"]}" + "-neoforge"
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