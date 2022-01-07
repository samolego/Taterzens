---
title: Spawn Eggs
---

# Spawn eggs

---

## Alternative to command spawning

Taterzens can be summoned using spawn eggs as well. What's more, you can even load presets
with spawn eggs.

???+ note
    Taterzen won't be selected if you spawn it with a spawn egg.

### Basic spawning
```brigadier
/give @s chicken_spawn_egg{EntityTag:{id:"taterzens:npc"}} 1
```

### Spawning with presets
Let's assume we have a [taterzen preset](../presets) named `my_npc`.
This command gives you a spawn egg that will spawn a taterzen from that preset.
```
/give @s chicken_spawn_egg{EntityTag:{id:"taterzens:npc"}, PresetOverride:"my_npc"} 1
```
To change the preset to load, edit the `PresetOverride` tag.
