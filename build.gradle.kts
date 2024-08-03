plugins {
    id("fabric-loom")
    kotlin("jvm") version "2.0.0"
    java
    `maven-publish`
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

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
    maven("https://maven.parchmentmc.org")
    maven("https://maven.quiltmc.org/repository/release/")
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

//    "mappings"(loom.layered {
//        mappings("org.quiltmc:quilt-mappings:${properties["minecraft_version"]}+build.${project.properties["quilt_mappings_version"]}:intermediary-v2")
//        officialMojangMappings()
//        parchment("org.parchmentmc.data:parchment-${properties["minecraft_version"]}:${project.properties["parchment"]}@zip")
//    })

    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_api_version"]}")

    include("io.wispforest:owo-sentinel:${properties["owo_version"]}")

    modImplementation("com.terraformersmc:modmenu:${properties["modmenu_version"]}") {
        exclude("net.fabricmc.fabric-api")
    }

    annotationProcessor("io.github.llamalad7:mixinextras-fabric:${properties["mixinextras_version"]}")?.let {
        implementation(it)
        include(it)
    }

    modImplementation("io.wispforest:owo-lib:${properties["owo_version"]}")

    modImplementation("io.wispforest:endec:${properties["endec_version"]}")!!.let(::include)
    modImplementation("io.wispforest.endec:netty:${properties["endec_netty_version"]}")!!.let(::include)
    modImplementation("io.wispforest.endec:gson:${properties["endec_gson_version"]}")!!.let(::include)
    modImplementation("io.wispforest.endec:jankson:${properties["endec_jankson_version"]}")!!.let(::include)

    implementation("com.google.devtools.ksp:symbol-processing-api:${properties["ksp_version"]}")
    implementation("com.squareup:kotlinpoet-ksp:${properties["kotlinpoet_version"]}")

    ksp("dev.kosmx.kowoconfig:ksp-owo-config:${properties["ksp_owo_config_version"]}")
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