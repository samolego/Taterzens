---
title: Right-click Actions
---


# Command Actions

---


## Right-click command actions

Taterzens support executing multiple commands
on click from version `0.3.0`.

Commands allow you to tp players, give them
stuff etc. Even more, you can target the Taterzen itself,
random player or all players!

NPCs can even edit themselves via commands that they execute! See the following video for example.

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/mxVgZmFcFPA" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

To see all the commands that will be run when Taterzen is right-clicked, use

```
/npc edit commands list
```


## Permission levels

(aka *Why did my command not work?*)

Default permission level for newly-spawned-in NPCs can be found in [config](../../installation/config.md).

If you'd like to keep a Taterzen to execute non-op
commands only, you'd type:
```
/npc edit commands setPermissionLevel 0
```

If you'd like to have `op` status, set their permission level to `4`
```
/npc edit commands setPermissionLevel 4
```

## Targeting the clicker

In most cases you'd probably like to target the player who clicked the NPC.
In theory you could use the `@p` target selector but that targets the *nearest
player*, who is not neccesarily the one who interacted with the Taterzen.
**Using `@s` will target Taterzen itself!**

Instead of that, you can use `--clicker--`, mod will replace any occurences with
the player's name when executing the command.

<br>

Giving the clicker a gold ingot:
```
/npc edit commands add minecraft give --clicker-- gold_ingot
```


Teleporting the clicker 10 blocks up:
```
/npc edit commands add minecraft execute at --clicker-- run tp --clicker-- ~ ~10 ~
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/PYkcRGhlwWw" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
