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

    modImplementation("maven.modrinth:owo-lib:${properties["owo_neo_version"]}") {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }

    forgeRuntimeLibrary("org.sinytra:forgified-fabric-loader:2.5.29+0.16.0+1.21:full")
    forgeRuntimeLibrary("org.sinytra.forgified-fabric-api:fabric-api-base:0.4.42+d1308dedd1") { exclude(group = "fabric-api")  }
    forgeRuntimeLibrary("org.sinytra.forgified-fabric-api:fabric-networking-api-v1:4.2.2+a92978fd19") { exclude(group = "fabric-api") }
    forgeRuntimeLibrary("org.sinytra.forgified-fabric-api:fabric-screen-api-v1:2.0.24+79a4c2b0d1") { exclude(group = "fabric-api") }

    // needed because architectury crashes out otherwise (strips jij)
    forgeRuntimeLibrary("io.wispforest:endec:0.1.8")
    forgeRuntimeLibrary("io.wispforest.endec:netty:0.1.4")
    forgeRuntimeLibrary("io.wispforest.endec:gson:0.1.5")
    forgeRuntimeLibrary("io.wispforest.endec:jankson:0.1.5")
    forgeRuntimeLibrary("blue.endless:jankson:1.2.2")

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