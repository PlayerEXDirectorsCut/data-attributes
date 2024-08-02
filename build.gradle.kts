import com.gradle.scan.agent.serialization.scan.serializer.kryo.fg
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    java
    kotlin("jvm") version "2.0.0"
    idea
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
        maven("https://maven.minecraftforge.net/")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
	    maven("https://maven.quiltmc.org/repository/release/")
        maven("https://maven.terraformersmc.com")
        maven("https://maven.wispforest.io/releases")
        maven("https://maven.kosmx.dev/")
        maven("https://maven.su5ed.dev/releases")
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        loom.silentMojangMappingsLicense()
        "mappings"(loom.layered {
            mappings("org.quiltmc:quilt-mappings:$minecraftVersion+build.${project.properties["quilt_mappings_version"]}:intermediary-v2")
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$minecraftVersion:${project.properties["parchment"]}@zip")
        })

        annotationProcessor("io.github.llamalad7:mixinextras-common:${project.properties["mixinextras_version"]}")

        compileOnly("org.jetbrains:annotations:24.1.0")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "idea")

    version = project.properties["mod_version"] as String
    group = project.properties["maven_group"] as String
    base.archivesName.set(project.properties["archives_base_name"] as String)

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    java.withSourcesJar()
}

