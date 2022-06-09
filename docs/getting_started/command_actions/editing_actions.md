---
title: Editing Actions
---


# Editing actions

---


## Oh no, I don't want that command anymore!

1. Check the command id by running
```
/npc edit commands list
```


<span style="color:aqua">
	Taterzen <span style="color:yellow">Taterzen</span> will execute the following commands on right-click.
</span>

<span style="color:gold">
	1-> give @r diamond <span style="color:red">X</span>
</span>

<span style="color:yellow">2-> tp -\-clicker-\- ~ ~10 ~</span>	<span style="color:red">X</span>

<span style="color:gold">
	3-> msg @a I'm broke now! <span style="color:red">X</span>
</span>


Then click on the command you'd like to remove or type
```
/npc edit commands group id <group id> remove <command id>
```


## Deleting all commands
To clear **all** the commands, run
```
/npc edit commands clear
```