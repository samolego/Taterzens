# Taterzens
[![Discord](https://img.shields.io/discord/797713290545332235?logo=discord&style=flat-square)](https://discord.gg/9PAesuHFnp)
[![GitHub license](https://img.shields.io/github/license/samolego/Taterzens?style=flat-square)](https://github.com/samolego/Taterzens/blob/master/LICENSE)
[![Server environment](https://img.shields.io/badge/Environment-server-blue?style=flat-square)](https://github.com/samolego/Taterzens)
[![Singleplayer environment](https://img.shields.io/badge/Environment-singleplayer-yellow?style=flat-square)](https://github.com/samolego/Taterzens)
[![Curseforge downloads](http://cf.way2muchnoise.eu/full_taterzens_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/taterzens)
[![Curseforge versions](https://cf.way2muchnoise.eu/versions/For%20MC_taterzens_all.svg)](https://www.curseforge.com/minecraft/mc-mods/taterzens)

A fabric / forge citizens like NPC mod.

You can find documentation [here](https://samolego.github.io/Taterzens/).

## Developers

Add `jitpack.io` maven repository.
```gradle
repositories {
    maven { url 'https://jitpack.io' }
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