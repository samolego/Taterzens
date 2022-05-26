---
title: Entity Types
---


# Entity Types

---


## Changing the type of Taterzen

**You'l need to install [DisguiseLib](https://modrinth.com/mod/disguiselib/versions) mod.**

Taterzens mod supports all vanilla entities (except for fishing bober).
Modded ones *should* work as well.

To change the type of the Taterzen, simply use the following command
```
/npc edit type <entity type>
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/OD84_yOOUoA" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Adding custom NBT

DisguiseLib supports disguises with nbt as well. To utilize it, append the nbt data at the end of the disguise command
```
/npc edit type <entity type> <nbt>
```

### Examples
???+ hint "Getting custom NBT"
	Head over to [mcstacker](https://mcstacker.net/) and select `summon` category. Provide the entity and set the wanted NBT. After that, copy the NBT only (the part in `{}`, including both brackets) and paste it after `<entity type>` in command above.

#### Armorer villager from desert
```
/npc edit type minecraft:villager {VillagerData:{profession:"minecraft:armorer",type:"minecraft:desert"}}
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/jrYyGCQgB-o" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

#### Baby chicken
```
/npc edit type minecraft:chicken {Age:-1}
```

#### Custom armor stand
???- hint "Easy armor stand customization"
	Patbox has made a cool armor stand editor that supports disguised entites as well. See [Armor Stand Editor](https://www.curseforge.com/minecraft/mc-mods/armor-stand-editor) for more info.

```
/npc edit type minecraft:armor_stand {HasVisualFire:1b,Glowing:0b,ShowArms:1b,Small:1b,NoBasePlate:1b}
```