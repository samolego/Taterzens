---
title: Movement
---


# Movement

---


## Taking care of fitness

Since Taterzens extend the Minecraft's vanilla `PathfinderMob`, they can
perform pathfinding.

Movement of Taterzens could be divided in 3 categories:

* [free movement](#free-movement) (Wandering around, random looking, following the path with rests)
* [strict movement](#strict-movement) (Non-stop following the path / looking at the player)
* [follow movement](#follow-movement) (Following specified targets)


If you don't want the Taterzen to move, you can use
`/npc edit movement NONE` (default).


## Strict Movement

### Looking at player

Makes the Taterzen look at a player in radious of 4 blocks
```
/npc edit movement FORCED_LOOK
```

=== "GUI"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/EofYSR-D4PI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

=== "Command"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/X9z0ykvXUUE" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

2. For following the [path](./path.md) strictly with no rests
```
/npc edit movement FORCED_PATH
```


## Follow Movement

1. Specifying any target
```
/npc edit movement FOLLOW <target to follow>
```


2. Specifying specific target
```
/npc edit movement FOLLOW UUID <uuid of the target to follow>
```

## Free Movement

1. If you have a non-escapable area, you can use the following command
```
/npc edit movement FREE
```
as it makes the Taterzen move around freely with no restrictions.

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/Mv3TnTVJ2aM" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>


2. For following the [path](./path.md) but enabling *long* rests and looking around, use
```
/npc edit movement PATH
```