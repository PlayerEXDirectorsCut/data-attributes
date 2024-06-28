plugins {
    id("fabric-loom")
    kotlin("jvm") version "1.8.20"
    java
    `maven-publish`
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

group = "${properties["maven_group"]}"
version = "${properties["mod_version"]}-${properties["loader"]}"

loom {
    runConfigs.configureEach {
        ideConfigGenerated(true)
    }
}

repositories {
    maven("https://maven.wispforest.io/releases")
    maven("https://maven.terraformersmc.com")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.kosmx.dev/")
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_api_version"]}")

    include("io.wispforest:owo-sentinel:${properties["owo_version"]}")

    include("io.wispforest:endec:${properties["endec_version"]}")!!.let(::api)
    include("io.wispforest.endec:netty:${properties["endec_version"]}")!!.let(::api)

    modImplementation("com.terraformersmc:modmenu:${properties["modmenu_version"]}") {
        exclude("net.fabricmc.fabric-api")
    }

    annotationProcessor("io.github.llamalad7:mixinextras-fabric:${properties["mixinextras_version"]}")?.let { implementation(it)?.let { include(it) } }

    modImplementation("io.wispforest:owo-lib:${properties["owo_version"]}")

    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.22")
    implementation("com.squareup:kotlinpoet-ksp:1.17.0")

    ksp("dev.kosmx.kowoconfig:ksp-owo-config:0.1.0")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }


    jar {
        from("LICENSE")
    }

    java {
        withSourcesJar()
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