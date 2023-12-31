# Data Attributes: Directors Cut

## Original (Modified) Description

### Maintained Repository

Please see updates, report issues, or contribute here: <https://github.com/PlayerEXDirectorsCut/data-attributes>

### What is Data Attributes?

Data Attributes is a Minecraft mod, initially released for Minecraft 1.17.1 using the Fabric ecosystem. The mod does two things: overhauls Minecraft's entity attribute system to be more dynamic and to include follow on attributes (something found in many other games); and exposes entity attributes to datapack manipulation - allowing servers/pack makers easy customisation of every aspect of the entity attribute system.

### Usage

Data Attributes has a [Curseforge](https://www.curseforge.com/minecraft/mc-mods/data-attributes-directors-cut) and [Modrinth](https://modrinth.com/mod/data-attributes-directors-cut) page. For developers, add the following to your `build.gradle`.

```gradle
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    modImplementation "maven.modrinth:data-attributes-directors-cut:<version>"
}
```

Alternatively, if you are using cursemaven:

```gradle
repositories {
    maven {
        name = "Cursemaven"
        url = "https://cursemaven.com"
    }
}

dependencies {
    modImplementation "curse.maven:data-attributes-directors-cut-955929:<version-file-id>"
}
```

Note that Data Attributes depends on [Fabric API](https://github.com/FabricMC/fabric), so you will need to consider this as well.

### F.A.Q

- Will you make a Forge version?
  - No, but the license allows you to do so yourself.
- I think that I've found a bug/crash, where can I report it?
  - Please make an entry to the [Issue Tracker](https://github.com/PlayerEXDirectorsCut/data-attributes/issues).

Please also note that the mod AttributeFix is incompatible with Data Attributes - they have the same capabilities!

### Wiki

For more detailed documentation, please see the [wiki](https://github.com/CleverNucleus/Data-Attributes/wiki).
