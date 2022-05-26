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

## Changing Default Sounds

You will need to open config file found in
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

Those are the default sounds a Taterzen will have after their creation. 
You can always remove sounds individually or completely (see the section 
about [Changing individual sounds](#changing-individual-sounds)).

## Changing individual sounds

The default sounds won't do anymore, and you are tired of your pet wolf NPC sounding like a villager?
Then the sound commands will relieve you of your pain. Currently, it is possible to edit sounds in the following three 
categories:
- Ambient Sounds: The sound your Taterzen will randomly make from time to time.
- Hurt Sounds: The sound your Taterzen will make when they are being attacked.
- Death Sounds: The sound your Taterzen will make when they die. 

To edit individual sounds of a Taterzen NPC you need to select them first. Afterwards you are provided with a variety of 
possibilities to gain information about the currently set sounds as well as to modify them.

### Retrieving currently set sounds for an individual Taterzen

Retrieve a list of currently set sounds with:

`/npc edit sounds list <all|ambient|hurt|death>`

For example, if you want to list the entries of all three sound categories at once, enter:

`/npc edit sounds list all`

If you desire the set sounds for, e.g., the hurt sounds instead, type:

`/npc edit sounds list hurt`

### Adding sounds to an individual Taterzen

You can add sounds to one of the previously mentioned three sound categories. The command looks like this:

`/npc edit sounds add <ambient|hurt|death> <resource>`

Make sure to use only valid sound resources. Otherwise, your Taterzen might remain quiet. It is possible that you get 
error messages if you do something wrong. But don't worry! Everything you do with these commands is reversible. 
You can utilize tab-completion suggestions, which show all available and valid sound resources. They can make your life 
easier.

For example, you want to make your Taterzen sound like a chicken:

`/npc edit sounds add ambient entity.chicken.ambient`

Or, if you are using tab-completion or just like to type out namespaces:

`/npc edit sounds add ambient minecraft:entity.chicken.ambient`


### Removing sounds from an individual Taterzen

In case you changed your mind or did something wrong, you can easily remove individual or all sounds from a Taterzen.

The removal command has the following structure:

`/npc edit sounds remove all|<<ambient|hurt|death> all|<<index|resource> <identifier>>>`

This looks, indeed, a bit complicated. Let's decompose the different cases and visualize them with some examples.

A little more decomposed, the command has a tree-like structure as follows:

```
/npc edit sounds remove all
                      |
                      -- <ambient|hurt|death> all
                                            |
                                            -- <index|resource> <identifier>
```

Here the lines only containing '|' leading downwards and starting with '--' represent alternatives, which you may enter 
after the word above the straight bar '|'.

Still confused? Let's have some examples:

Assume you want to remove __*all*__ sounds from __*all*__ sound categories from your selected Taterzen. In that case 
you can simply do:

`/npc edit sounds remove all`

If you want to remove __*all*__ sounds from a __*specific*__ sound category instead, and this category is for example 
the death sound category, then this command will fulfill that job:

`/npc edit sounds remove death all`

Consider another case: You would like to remove a __*specific*__ sound from a __*specific*__ sound category, then 
you've got two possibilities. Assume that our output of

`/npc edit sounds list all`

yields:

```
Currently set ambient sounds:
1. minecraft:entity.chicken.ambient
2. minecraft:entity.villager.ambient
Currently set hurt sounds: No sounds set.
Currently set death sounds: No sounds set
```

and we want to get rid of the villager sound.

Now you can either remove the sound using the index (i.e. the number) of the corresponding list entry:
 
`/npc edit sounds remove ambient index 2`

Or you can remove the sound using its name:

`/npc edit sounds remove ambient resource minecraft:entity.villager.ambient`

Alternatively without the namespace:

`/npc edit sounds remove ambient resource entity.villager.ambient`

Note that for addressing the sound with the index, you need the `index` keyword after you specified the sound category, 
while you have to use the `resource` keyword in order to remove the sound by using its name. (Technically the name 
represents a so-called _resource location_. See Minecraft-Wiki: https://minecraft.fandom.com/wiki/Resource_location )

_Remember:_ Tab-completion makes your life easier!