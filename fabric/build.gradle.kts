plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentFabric").extendsFrom(configurations["common"])
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

// Fabric Datagen Gradle config.  Remove if not using Fabric datagen
fabricApi.configureDataGeneration()

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}+$minecraftVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.properties["fabric_language_kotlin_version"]}")

    annotationProcessor("io.github.llamalad7:mixinextras-fabric:${project.properties["mixinextras_version"]}")?.let {
        implementation(it)
        include(it)
    }

    include("io.wispforest:owo-sentinel:${project.properties["owo_version"]}")
    modImplementation("io.wispforest:owo-lib:${project.properties["owo_version"]}")

    modImplementation("io.wispforest:endec:${project.properties["endec_version"]}")!!.let(::include)
    modImplementation("io.wispforest.endec:netty:${project.properties["endec_netty_version"]}")!!.let(::include)

    implementation("com.google.devtools.ksp:symbol-processing-api:${project.properties["ksp_version"]}")
    implementation("com.squareup:kotlinpoet-ksp:${project.properties["kotlinpoet_version"]}")

    ksp("dev.kosmx.kowoconfig:ksp-owo-config:${project.properties["ksp_owo_config_version"]}")

    modImplementation("com.terraformersmc:modmenu:${project.properties["modmenu_version"]}") {
        exclude("net.fabricmc.fabric-api")
    }

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }
}

tasks {
    base.archivesName.set(base.archivesName.get() + "-Fabric")
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
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

components {
    java.run {
        if (this is AdhocComponentWithVariants)
            withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) { skip() }
    }
}

publishing {
    publications.create<MavenPublication>("mavenFabric") {
        artifactId = "${project.properties["archives_base_name"]}" + "-Fabric"
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