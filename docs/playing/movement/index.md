---
layout: page
title: Movement
parent: Play
has_children: true
nav_order: 4
---


# Movement

---


## Taking care of fitness

Since Taterzens extend the Minecraft's vanilla `PathAwareEntity`, they can
perform pathfinding.

Movement of Taterzens could be divided in 3 categories:
* free movement (Wandering around, random looking, following the path with rests)
* strict movement (Non-stop following the path / looking at the player)
* follow movement (following specified targets)


If you don't want the Taterzen to move, you can use
`/npc edit movement NONE` (default).