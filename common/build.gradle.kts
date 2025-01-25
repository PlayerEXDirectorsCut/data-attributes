import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

architectury {
    common("fabric", "neoforge")
    platformSetupLoomIde()
}

val minecraftVersion = project.properties["minecraft_version"] as String

val fabricApiModules = setOf(
    "fabric-api-base",
    "fabric-command-api-v1",
    "fabric-lifecycle-events-v1",
    "fabric-networking-api-v1",
    "fabric-resource-loader-v0",
    "fabric-entity-events-v1"
)

loom.accessWidenerPath.set(file("src/main/resources/data_attributes.accesswidener"))

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

repositories {
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${properties["fabric_loader_version"]}")

    fabricApiModules.forEach {
        modApi(fabricApi.module(it, "${project.properties["fabric_api_version"]}+$minecraftVersion"))
    }

    modApi("io.wispforest:owo-lib:${properties["owo_version"]}")

    ksp("dev.kosmx.kowoconfig:ksp-owo-config:${properties["ksp_owo_config_version"]}")
}

publishing {
    publications.create<MavenPublication>("mavenCommon") {
        artifactId = "${project.properties["archives_base_name"]}" + "-Common"
        from(components["java"])
    }

    repositories {
        mavenLocal()
//        maven {
//            val releasesRepoUrl = "https://example.com/releases"
//            val snapshotsRepoUrl = "https://example.com/snapshots"
//            url = uri(if (project.version.toString().endsWith("SNAPSHOT") || project.version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
//            name = "ExampleRepo"
//            credentials {
//                username = project.properties["repoLogin"]?.toString()
//                password = project.properties["repoPassword"]?.toString()
//            }
//        }
    }
}
