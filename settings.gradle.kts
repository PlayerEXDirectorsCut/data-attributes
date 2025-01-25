pluginManagement.repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.neoforged.net/")
    gradlePluginPortal()
}

plugins {
    id("com.gradle.develocity") version "3.17.4"
}

develocity.buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
}

include("common", "fabric", "neoforge")

rootProject.name = "Data Attributes"
