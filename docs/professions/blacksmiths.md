---
title: Blacksmiths
---

A Taterzen addon that brings tool-repairing-guys to Minecraft.

[![CurseForge](https://cf.way2muchnoise.eu/versions/For%20MC_550916_all.svg?style=flat-square)](https://www.curseforge.com/minecraft/mc-mods/blacksmiths)
[![Stars](https://img.shields.io/github/stars/samolego/Blacksmiths?style=flat-square)](https://github.com/samolego/Blacksmiths)

# Blacksmiths

Blacksmiths addon allows you to create Taterzens which can repair tools.
They can be configured how much durability per second they repair, whether they "work" in unloaded chunks, etc.

<video controls="true" allowfullscreen="true" width="100%">
	<source src="../../assets/video/blacksmith_profession_showcase.mp4" type="video/mp4">
	<p>Your browser does not support the video element.</p>
</video>

## Usage

Make sure to [assign](./assigning_professions.md#giving-taterzen-a-profession) the `blacksmiths:blacksmith` profession to your Taterzen.
After that, simply use right click on the Taterzen to open the GUI.


<video controls="true" allowfullscreen="true" width="100%">
	<source src="../../assets/video/adding_blacksmith_profeesion.mp4" type="video/mp4">
	<p>Your browser does not support the video element.</p>
</video>

## Pricing

If you install [Grand Economy](https://www.curseforge.com/minecraft/mc-mods/grand-economy) mod, you can use it instead of defualt item pricing.
Otherwise you can set the default payment item and how much is it "worth".

<video controls="true" allowfullscreen="true" width="100%">
	<source src="../../assets/video/grand_economy_impl.mp4" type="video/mp4">
	<p>Your browser does not support the video element.</p>
</video>

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
