---
title: Skins
---


# Changing skins
![FabricTailor logo](https://cdn.modrinth.com/data/g8w1NapE/icon.png){: width="150px"}

---


## Default (built-in) skin swapping

* By player name.

Taterzens mod supports setting custom skins to NPCs
if their [type](./types.md) is set to `PLAYER`.
All skins are **cached** after being set, meaning that they
will stay the same *even if player that skin was fetched from changes their skin*.
You can set any skin from any minecraft player using
```
/npc edit skin <minecraft player name>
```

* From [mineskin](https://www.mineskin.org).
	1. Visit [mineskin.org](https://www.mineskin.org) site
	2. Upload the skin you want for taterzen / use a player name.
	3. Click on generate & copy URL (or scroll down, then choose `Taterzens` and copy the command)
	![Mineskin](../../assets/img/mineskin.png)
	4. Enter the command
	```
	/npc edit skin <your mineskin URL>
	```
	E.g.
	```
	/npc edit skin  https://www.mineskin.org/1234
	```



## Skin layers
To set custom skin layers to Taterzen, use
```
/npc edit skin
```
It will copy your skin settings to Taterzen.

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/GC2O78TZMy4" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Custom skins

*This is similar to mineskin section.*


To use the skins to their fullest potential, you will need
to install [FabricTailor](https://modrinth.com/mod/FabricTailor) mod.
After installing it, *select* the desired NPC you wish to change skin for.

When having a Taterzen selected, simply run FabricTailor's [`/skin`](https://github.com/samolego/FabricTailor/wiki) command
```
/skin set <follow the wiki or brigadier suggestions>
```
