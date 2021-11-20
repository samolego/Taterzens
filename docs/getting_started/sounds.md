---
title: Sounds
---


# Changing sounds

---


## Those sounds are annoying

Wanna turn off the sounds for the Taterzen? Simply type

```
/npc edit tags allowSounds false
```

this will mute Taterzen.

## Changing global sounds

You'll need to open config file found in
`.minecraft/config/Taterzens/config.json`.

Open it with any text editor (although I recommend something like Notepad++) and edit
the following values:

```
"death_sounds": [
    "my.custom.sound",
    "another.custom.sound"
],
"hurt_sounds": [
    "entity.player.hurt"
],
"ambient_sounds": []

```

Empty array (`[]`) means no sound. If there is more
than one sound, it will be chosen randomly.
