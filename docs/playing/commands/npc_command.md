---
layout: page
title: npc command
parent: Commands
grand_parent: Play
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
You can select NPCs by their ID or based on proximity and look direction.

#### **Id**
1. Run `/npc list` to view the IDs of available NPCs.
2. Run `/npc select id <ID>` to select it.

#### **Distance**
1. Stand in front of the NPC you want to select.
2. Run `/npc select` to select it.


### Editing

You can change many aspects of the NPC, e.g. its entity type, skin, right-click action etc.

todo
{: .label .label-yellow }

### Auto-generated structure

This might help you understand the layout of the `/npc` command.

<span style="color: cyan;">
    (For permission nodes described here, have a look [on permissions site](../../configurations/permissions.html).)
</span>


<span style="color: lightgreen;">
    &lt;These represent arguments&gt;
</span>

<div class="language-plaintext highlighter-rouge highlight">
    <pre><code id="npcCommand">Loading ... </code></pre>
</div>

<script src="./auto_command.js"></script>
