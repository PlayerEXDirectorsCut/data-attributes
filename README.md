![Data Attributes Banner](https://cdn.modrinth.com/data/cached_images/464354cc9d34d3778ad4a9db3816adc86c0f6b84.png)
[![GitHub license](https://img.shields.io/badge/MIT-MIT?style=for-the-badge&label=LICENCE&labelColor=1A1A1A&color=FFFFFF&link=https%3A%2F%2Fgithub.com%2FPlayerEXDirectorsCut%2Fdata-attributes%2Fblob%2F1.20.1%2Fmain%2FLICENSE)](https://github.com/PlayerEXDirectorsCut/data-attributes/blob/1.20.1/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/PlayerEXDirectorsCut/data-attributes?style=for-the-badge&logo=github&labelColor=1A1A1A&color=FFFFFF&link=https%3A%2F%2Fgithub.com%2FPlayerEXDirectorsCut%2Fdata-attributes%2Fstargazers
)](https://github.com/PlayerEXDirectorsCut/data-attributes/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/PlayerEXDirectorsCut/data-attributes?style=for-the-badge&logo=github&labelColor=1A1A1A&color=FFFFFF&link=https%3A%2F%2Fgithub.com%2FPlayerEXDirectorsCut%2Fdata-attributes%2Fforks
)](https://github.com/PlayerEXDirectorsCut/data-attributes/forks)
[![GitHub issues](https://img.shields.io/github/issues/PlayerEXDirectorsCut/data-attributes?style=for-the-badge&logo=github&label=ISSUES&labelColor=1A1A1A&link=https%3A%2F%2Fgithub.com%2FPlayerEXDirectorsCut%2Fdata-attributes%2Fissues
)](https://github.com/PlayerEXDirectorsCut/data-attributes/issues)

[![docs](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/generic_vector.svg)](https://playerexdirectorscut.github.io/Bare-Minimum-Docs/)
![mkdocs](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/mkdocs_vector.svg)
![java17](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/built-with/java17_vector.svg)
[![curseforge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_vector.svg)](https://www.curseforge.com/minecraft/mc-mods/data-attributes-directors-cut)
[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/mod/data-attributes-directors-cut)

## Preamble

**Data Attributes: Directors Cut** is a Minecraft mod that has been ported from 1.19.2. **[Original Mod](https://www.curseforge.com/minecraft/mc-mods/data-attributes)**

It serves two functions:
- Overhauling Minecraft's Entity Attribute system to be more **dynamic**
- Allowing attributes to be **exposed to datapack manipulation**, so it's easy for pack developers to customize everything about the attribute system

## Usage

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

<details><summary>Alternatively, if you are using cursemaven:</summary>

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

</details>

Note that Data Attributes: Directors Cut depends on [Fabric API](https://github.com/FabricMC/fabric), so you will need to consider this as well.

It also does not support `Attributefix`, as they have the same capabilities, and are incompatible.

### • F.A.Q
- I think that I've found a bug/crash, where can I report it?
    - Please make an entry to the [Issue Tracker](https://github.com/PlayerEXDirectorsCut/data-attributes/issues).
