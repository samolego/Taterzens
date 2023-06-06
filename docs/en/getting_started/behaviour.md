---
title: Changing Behaviour
---


# Behaviour editing

---


## Changing the behaviour

???+ "Getting UUID"
    An easy way to get the uuid of a Taterzen is to have your crosshairs focused on the Taterzen when typing out the command. Then, you can use tab completion and the uuid will be suggested.

Default Taterzen behaviour will be [passive](#passive-behaviour).
*~~It's up to you whether you dare to change it.~~*

If you use any other types than the default one, you'd
probably want to change their invulnerable status as well.
To achieve that, use
```
/data merge entity <taterzen uuid> {Invulnerable:1b}
```

* 1b = true
* 0b = false

## Teaming them up

Taterzen teams use the vanilla teams system. For extra docs on the team command, see the [wiki](https://minecraft.fandom.com/wiki/Commands/team#Syntax).
To create and add taterzens to a team, you can simply do
```
/team add teamName
/team join teamName <taterzen uuid>
```

Taterzens **will never attack another entity on the same team**, but if you're not on their team, watch out!

## Hostile Behaviour

*It was a lovely day until I wrote*
```
/npc edit behaviour HOSTILE
```
*in chat.*

*The NPC has started attacking all living
entites. I had to run away,
otherwise I'd be its next prey.
**Don't make the same mistake.** I don't even know why I told you
the above line ...*

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/i15mTwF14XI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Defensive Behaviour

* The defensive mechanism will make Taterzen target
the mob that attacked it.
```
/npc edit behaviour DEFENSIVE
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/NcajBNITVtc" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Friendly behaviour

* Taterzens with friendly behaviour will target any
monsters that dare to come around.
```
/npc edit behaviour FRIENDLY
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/P32Th75uj4Q" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Passive behaviour

* If you have problems with Taterzen being too aggressive, use
```
/npc edit behaviour PASSIVE
```
It will calm down the selected one.
