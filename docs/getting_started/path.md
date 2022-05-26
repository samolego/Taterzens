---
title: Path
---


# Path

---


## Path

![Path showcase](../assets/img/path_showcase.gif)

## Setting path nodes

Path nodes are targets where Taterzen will move to.
To edit those, you have two possibilities for that: interactive or command based. It is also possible to mix those 
editing methods. 

### Interactive method

If you would like to edit the nodes in an interactive manner, i.e. with visual feedback in the world and by clicking on 
blocks in your environment to set or remove nodes, you need to enter the path editor by running:
```
/npc edit path
```
To exit the editor, run the same command again.


#### 1. Adding nodes

To add a block as a node to the Taterzen's path, simply left click on it. It will temporarily be switched to a redstone 
block to aid you during the path building process. But don't worry, this will be reversed as soon as you leave the 
editor.

#### 2. Removing nodes

To remove a node from the path, just right click on it.

### Command based method

You can add or remove blocks using commands. If you would like to benefit from the visual feedback provided in the 
interactive method you can just enter the interactive editor as described in section [Interactive method](#interactive-method)
and run your commands. (Or click on blocks if you prefer that from time to time.)

#### 1. Listing currently set path nodes

If you want to retrieve an ordered list of path nodes (which will be the coordinates you set), use the following command:
```
/npc edit path list
```

You will get an output looking something like this _(in case you have already set a path for the selected Taterzen)_:
```
Currently set path nodes:
1: (24, 72, -11)
2: (29, 71, -7)
3: (27, 73, -1)
4: (31, 72, -9)
5: (28, 72, -15)
6: (24, 72, -4)
```
The numbers within the smooth brackets, e.g. `(24, 72, -11)`, are the x, y and z coordinates of the respective path 
node. (See also: [Minecraft Wiki on Coordinates](https://minecraft.fandom.com/wiki/Coordinates))

The numbers in front of the coordinates are the _index numbers_ of the corresponding coordinate-list-entry. You will 
refer to those index numbers in case you want to remove nodes via commands.

#### 2. Adding nodes

Adding nodes via a command is simple. You just run the command:

```
/npc edit path add <pos>
```

and provide a valid block position for `<pos>`. A valid block position means: The block is currently loaded and in the 
same dimension as you are. 

The position `<pos>` just consists of three coordinate values for x, y, and z. You can utilize 
[relative](https://minecraft.fandom.com/wiki/Coordinates#Relative_world_coordinates) and 
[local](https://minecraft.fandom.com/wiki/Coordinates#Local_coordinates) coordinate descriptions from vanilla minecraft. 
(I.e. `~ ~ ~` or `^ ^ ^`).

For example let's say you want to add a node at position `x = 29, y = 71, z = -7`. This can be done like this:
```
/npc edit path add 29 71 -7
```

Another example: if you want to add the block you are standing on, do:
```
/npc edit path add ~ ~-1 ~
```

#### 3. Removing nodes

In order to remove nodes via commands you can either remove the most recently set node (i.e. the last node in the list), 
or you can remove specific nodes via their list entry index number.

To remove the most recent node just run:
```
/npc edit path remove
```

If you want to get rid of a specific node in your list instead, the command has this structure:
```
/npc edit path remove index <index>
```

For example, let's say you want to remove node number 3 from your path:
```
/npc edit path remove index 3
```
_Note:_ This node will be removed and all other nodes after node number 3, i.e. nodes 4, 5, 6, etc., move "one position 
up", which means that their list index will be lowered by 1. Consequently, node 4 will be the new node number 3, node 5 
will be the new number 4 and so on. Keep that in mind when removing nodes via indices!

You can also use tab-completion suggestions, where currently available list indices are provided.
Don't worry if you make a mistake and are going to remove an entry which does not exist, i.e. if the index you pass to 
the command is lower than 1 or greater than the last available list index. You will just get an error message in that 
case, helping you to prevent doing that mistake again. If the list is empty anyway you will get a confirmation message 
accordingly.

#### 4. Clearing the whole path
To remove all nodes, run
```
/npc edit path clear
```
You'd probably want to set the movement to `NONE` as well.

<video controls="true" allowfullscreen="true" width="100%">
	<source src="../../assets/video/path_creation.mp4" type="video/mp4">
	<p>Your browser does not support the video element.</p>
</video>