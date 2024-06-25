import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    id("fabric-loom") version "1.7-SNAPSHOT"
    kotlin("kapt") version "2.0.0"
    `maven-publish`
    java
}

group = "${properties["maven_group"]}"
version = "${properties["mod_version"]}-${properties["loader"]}"

repositories {
    maven("https://maven.wispforest.io")
    maven("https://maven.terraformersmc.com")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_api_version"]}")

    modImplementation("io.wispforest:owo-lib:${properties["owo_version"]}")?.let {
        annotationProcessor(it)
        kapt(it)
    }
    include("io.wispforest:owo-sentinel:${properties["owo_version"]}")

    implementation("io.wispforest:endec:${properties["endec_version"]}")?.let(::include)
    implementation("io.wispforest.endec:gson:${properties["endec_version_2"]}")?.let(::include)
    implementation("io.wispforest.endec:codec:${properties["endec_version_2"]}")?.let(::include)
    implementation("io.wispforest.endec:netty:${properties["endec_version_2"]}")?.let(::include)

    implementation("io.github.llamalad7:mixinextras-fabric:${properties["mixinextras_version"]}")?.let {
        include(it)
        annotationProcessor(it)
    }

    modImplementation("com.terraformersmc:modmenu:${properties["modmenu_version"]}") {
        exclude("net.fabricmc.fabric-api")
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        repositories {}
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}