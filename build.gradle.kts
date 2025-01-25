import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    java
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    idea
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

val minecraftVersion = project.properties["minecraft_version"] as String

architectury.minecraft = minecraftVersion

subprojects {
    apply(plugin = "dev.architectury.loom")

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.parchmentmc.org")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
	    maven("https://maven.quiltmc.org/repository/release/")
        maven("https://maven.kosmx.dev/")
        maven("https://maven.wispforest.io/releases")
        maven("https://maven.terraformersmc.com")
        maven("https://api.modrinth.com/maven")
        maven("https://maven.su5ed.dev/releases")
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        loom.silentMojangMappingsLicense()
        "mappings"(loom.layered {
            mappings("org.quiltmc:quilt-mappings:${project.properties["quilt_mappings_minecraft_version"]}+build.${project.properties["quilt_mappings_version"]}:intermediary-v2")
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$minecraftVersion:${project.properties["parchment"]}@zip")
        })

        implementation("com.google.devtools.ksp:symbol-processing-api:${properties["ksp_version"]}")
        implementation("com.squareup:kotlinpoet-ksp:${properties["kotlinpoet_version"]}")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

        compileOnly("org.jetbrains:annotations:24.1.0")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "idea")
    apply(plugin = "com.google.devtools.ksp")

    version = project.properties["mod_version"] as String
    group = project.properties["maven_group"] as String
    base.archivesName.set(project.properties["archives_base_name"] as String)

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    java.withSourcesJar()
}

