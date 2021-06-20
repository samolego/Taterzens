---
layout: page
title: Adding Command Actions
parent: Right-click Actions
grand_parent: Play
---


# New right-click command action

---


## Adding new command to Taterzen

It's as simple as using
```
/npc edit commands add <command>
```

*Note: all commands that are added are
executed **AT ONCE** when NPC is interacted with.*

<video controls="true" allowfullscreen="true" width="100%">
	<source src="https://samolego.github.io/Taterzens/docs/assets/video/commands.mp4" type="video/mp4">
	<p>Your browser does not support the video element.</p>
</video>

## I want to target the player who interacted with my NPC!

You're in luck! There is built-in `--clicker--` target,
which is replaced by the name of the player that has
right clicked the NPC.

Let's suppose player with name `samo_lego` interacts with the NPC
that we previously added the following command:
```
/npc edit commands add give --clicker-- sunflower{display:{Name:'{"text":"--clicker--'s coin"}'}}
```

The command Taterzen will execute will change to the following:
```
/give samo_lego sunflower{display:{Name:'{"text":"samo_lego's coin"}'}} 1
```

And our player will get a `Sunflower` item with the name
`samo_lego's coin`.
