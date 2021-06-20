---
layout: page
title: Skins
parent: Play
---


# Changing skins
![FabricTailor logo](https://cdn.modrinth.com/data/g8w1NapE/icon.png){: width="150px"}

---


## Default (built-in) skin swapping

* By player name.

Taterzens mod supports setting custom skins to NPCs
if their [type](./types.html) is set to `PLAYER`.
All skins are **cached** after being set, meaning that they
will stay the same *even if player that skin was fetched from changes their skin*.
You can set any skin from any minecraft player using
```
/npc edit skin <minecraft player name>
```

* From [mineskin](https://www.mineskin.org).
1. Visit [mineskin.org](https://www.mineskin.org) site
2. Upload the skin you want for taterzen / use a player name.
3. Click on generate & copy URL
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

<video controls="true" allowfullscreen="true" poster="../assets/img/skin_layer_change_poster.png" width="100%">
	<source src="https://samolego.github.io/Taterzens/docs/assets/video/skin_layer_change.mp4" type="video/mp4">
	<p>Your browser does not support the video element.</p>
</video>


## Custom skins

*This is similar to mineskin section.*


To use the skins to their fullest potential, you will need
to install [FabricTailor](https://modrinth.com/mod/FabricTailor) mod.
After installing it, *select* the desired NPC you wish to change skin for.

When having a Taterzen selected, simply run FabricTailor's [`/skin`](https://github.com/samolego/FabricTailor/wiki) command
```
/skin set <follow the wiki or brigadier suggestions>
```
