---
title: Blacksmiths
---

A Taterzen addon that brings tool-repairing-guys to Minecraft.

[![CurseForge](https://cf.way2muchnoise.eu/versions/For%20MC_550916_all.svg?style=flat-square)](https://www.curseforge.com/minecraft/mc-mods/blacksmiths)
[![Stars](https://img.shields.io/github/stars/samolego/Blacksmiths?style=flat-square)](https://github.com/samolego/Blacksmiths)

# Blacksmiths

Blacksmiths addon allows you to create Taterzens which can repair tools.
They can be configured how much durability per second they repair, whether they "work" in unloaded chunks, etc.

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/RrduZUPcmfY" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Usage

Make sure to [assign](./assigning_professions.md#giving-taterzen-a-profession) the `blacksmiths:blacksmith` profession to your Taterzen.
After that, simply use right click on the Taterzen to open the GUI.

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/9gJV5l_lSlI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Pricing

If you install [Grand Economy](https://www.curseforge.com/minecraft/mc-mods/grand-economy) mod, you can use it instead of default item pricing.
Otherwise you can set the default payment item and how much is it "worth".

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/L8c5hZvJOBU" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Per-taterzen configuration

Do you want your players to have taterzens with different repairing values? Use the following command:

```
/profession blacksmith <rule>
```

You can use the following rules:

* `costPerDurabilityPoint` - how much does it cost to repair 1 durability point.
* `durabilityPerSecond` - how much durability is repaired each second (can be less than `1` as well).
* `workInUnloadedChunks` - whether the taterzen works in unloaded chunks.

## Wait ... unloaded chunks?

Yes, you've read it right. As Blackmiths operate via system time, they are not
dependant on the server tick rate. What's more, your tools can be repaired even
when your pc is off :smile:.
