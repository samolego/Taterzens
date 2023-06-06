---
title: npc command
---

# `npc` command

---

## What does it do?

It helps you manage the NPCs.

Make sure you have selected the NPC before doing edits.

### Creating
`/npc create <name of the NPC>` - Creates a Taterzen NPC and selects it.

### Removing
`/npc remove` - Removes the **selected** Taterzen NPC.

### Teleporting
`/npc tp <destination>` - Teleports the selected NPC to the desired location.
* `<destination>` can be an entity or a set of relative / absolute coordinates

### Listing
`/npc list` - Lists available (loaded) NPCs.

### Selecting
You can select NPCs by their ID, name, UUID or based on proximity and look direction.

_Note:_ The ID means the list index number which is shown when you run the `/npc list` command. 
(The first number before the name.) Contrary, the UUID is the _**u**niversally **u**nique **id**entifier_
as used internally by Minecraft to uniquely distinguish between entities. 
(See Minecraft Wiki: https://minecraft.fandom.com/wiki/Universally_unique_identifier )

#### **Id**
1. Run `/npc list` to view the IDs of available NPCs.
2. Run `/npc select id <ID>` to select it.

#### **UUID**
1. Run `/npc list` to view the list of available NPCs.
2. With the command prompt (the chat) being open, hover with the mouse over `(UUID)` after the name of the NPC you want 
to select. 
3. Click on the `(UUID)` text in the list.
4. The UUID of your NPC will be displayed in the chat input. Select it and copy it to clipboard (usually CTRL and C 
on Windows.)
5. Run `/npc select uuid <UUID>`, paste the `<UUID>` from clipboard (with CTRL and V on Windows). Alternatively use 
one of the tab-completion suggestions.
6. The Npc is then selected.

#### **Name**
1. Run `/npc list` to view the list of available NPCs and their names.
2. Run `/npc select name <NAME>` to select your desired NPC. Alternatively use one of the names from the tab-completion 
suggestions.

_Note:_ In case the name of your NPC contains whitespaces (e.g. something like "Steve the Dragonslayer") make sure to 
enclose the name in quotation marks. In our example: `/npc select name "Steve the Dragonslayer"`. Without the quotation 
marks the NPC won't be selected.

_Note:_ If you have more than one NPC with the same name, no NPC will be selected and an error message will be 
displayed.

#### **Distance**
1. Stand in front of the NPC you want to select.
2. Run `/npc select` to select it.


### Editing

You can change many aspects of the NPC, e.g. its entity type, skin, pose, name, messages, right-click actions etc.
