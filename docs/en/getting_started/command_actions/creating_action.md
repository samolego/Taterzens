---
title: Adding Command Actions
---


# New right-click command action

---


## Adding new command to Taterzen

It's as simple as using
```
/npc edit commands add minecraft <command>
```

*Note: all commands that are added to the group are
executed **AT ONCE** when NPC is interacted with.*

Each group of commands is executed on right click.

When npc is clicked the second time, new group is selected and its commands are executed.
<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/ygkj7WZlhq0" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
## I want to target the player who interacted with my NPC!

Nothing easier! There is built-in `--clicker--` target,
which is replaced by the name of the player that has
right clicked the NPC.

Let's suppose player with name `samo_lego` interacts with the NPC
that we previously added the following command:
```
/npc edit commands add minecraft give --clicker-- sunflower{display:{Name:'{"text":"--clicker--'s coin"}'}}
```

The command Taterzen will execute will change to the following:
```
/give samo_lego sunflower{display:{Name:'{"text":"samo_lego's coin"}'}} 1
```

And our player will get a `Sunflower` item with the name
`samo_lego's coin`.
