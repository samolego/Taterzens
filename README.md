[![Taterzens logo](https://raw.githubusercontent.com/samolego/Taterzens/master/docs/assets/img/banner.svg)](https://samolego.github.io/Taterzens/latest)

[![Discord](https://img.shields.io/discord/797713290545332235?logo=discord&style=flat-square)](https://discord.gg/9PAesuHFnp)
[![GitHub license](https://img.shields.io/github/license/samolego/Taterzens?style=flat-square)](https://github.com/samolego/Taterzens/blob/master/LICENSE)
[![Server environment](https://img.shields.io/badge/Environment-server-blue?style=flat-square)](https://github.com/samolego/Taterzens)
[![Singleplayer environment](https://img.shields.io/badge/Environment-singleplayer-yellow?style=flat-square)](https://github.com/samolego/Taterzens)

Fabric: [![Curseforge downloads](http://cf.way2muchnoise.eu/full_446499_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/taterzens)
[![Curseforge versions](http://cf.way2muchnoise.eu/versions/For_446499_all.svg)](https://www.curseforge.com/minecraft/mc-mods/taterzens)

Forge: [![Curseforge downloads](http://cf.way2muchnoise.eu/full_473071_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/taterzens-forge)
[![Curseforge versions](http://cf.way2muchnoise.eu/versions/For_473071_all.svg)](https://www.curseforge.com/minecraft/mc-mods/taterzens-forge)

A fabric / forge citizens like NPC mod.

# **-> *[Documentation](https://samolego.github.io/Taterzens/)* <-**


https://user-images.githubusercontent.com/34912839/142493879-6f642eb1-e226-4798-abfd-ef190f7fe4a8.mp4

## Addons
* [Traders](https://github.com/samolego/TraderNPCs)
* [Blacksmiths](https://github.com/samolego/Blacksmiths)

## Thanks
* [SGUI mod by Patbox](https://github.com/Patbox/sgui) (<- make sure to give this a star!)

## Translation contributions
1. Fork the repository
2. Add [language file](https://github.com/samolego/Taterzens/tree/master/common/src/main/resources/data/taterzens/lang) (make sure to follow the [Minecraft language codes](https://minecraft.gamepedia.com/Language)) as json.
    1. Name it e.g. `en_us.json` for American English
    2. Translate the messages
3. Commit the file and submit a PR :wink:



## Developers

### Contributing

**Warning!**

Taterzens mod uses **[ParchmentMC](https://parchmentmc.org/) mappings.**
If you're a [Yarn](https://github.com/FabricMC/yarn) contributor, you might **not want** to see the source!

### Dependency
Add `jitpack.io` maven repository.
```gradle
repositories {
    maven { url 'https://jitpack.io' }
    maven {
        // LuckPerms maven
        url 'https://oss.sonatype.org/content/repositories/snapshots'
    }
}
```

Depending on the modloader, add Taterzens as a dependency. Replace the `[LATEST_VERSION]` with the one found [here](https://github.com/samolego/Taterzens/releases/latest).
```gradle
dependencies {
    // Architectury (common module)
    modImplementation 'com.github.samolego.Taterzens:taterzens:[LATEST_VERSION]'
    
    // Fabric
    modImplementation 'com.github.samolego.Taterzens:taterzens-fabric:[LATEST_VERSION]'
    
    // Forge
    implementation fg.deobf 'com.github.samolego.Taterzens:taterzens-forge:[LATEST_VERSION]'
}
```
