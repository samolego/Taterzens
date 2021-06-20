---
layout: page
title: Profession Info
parent: Profession API
nav_order: 0
---


# Dealing with professions (in-game)

---


## Adding profession(s) to Taterzen
```
/npc edit professions add <profession id>
```
Profession IDs will be automatically generated for each profession
that was registered by a mod developer. By default, there are no
profession mods present, but this might change in future :).

## Listing current professions
```
/npc edit professions list
```
This will print all the professions of Taterzen to chat.
You can click on the <span style="color:red">X</span> to delete profession from Taterzen.

## Removing professions
As said above, you can click on the <span style="color:red">X</span> next to the profession
identifier to remove it. But you can also use
```
/npc edit professions remove <profession id>
```
