---
title: 声音
---


# 更改声音

---


## 烦人的声音

想要彻底关闭NPC的声音吗？只需要输入以下指令：

```
/npc edit tags allowSounds false
```

NPC将会彻底静音！

## 更改默认声音

需要打开配置文件，配置文件的路径是`.minecraft/config/Taterzens/config.json`。

使用任意文本编辑器打开配置文件都可以，只不过我这边推荐使用“Visual Studio Code”。

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

设置为`[]`代表没有任何声音，如果存在多种声音，那么将会随机选择其中一种。

这些都是本模组的默认声音，你完全可以随时单独或者完全删除声音，详细请参考[更改单个声音](#changing-individual-sounds)。

## 更改单个声音

如果你觉得默认的声音不好听，或者说NPC宠物的声音听起来好像村民，那么可以试试使用一下三种办法来编辑声音，降低听觉痛苦：
类型：
- 环境声音（ambient）：NPC会偶尔发出环境的随机声音。
- 受伤声音（hurt）：当NPC受到攻击时，NPC会发出的声音。
- 死亡声音（death）：当NPC死亡时会发出的声音。 

要想编辑单个NPC的声音，需要先选择NPC，然后可以在该NPC的声音编辑界面中修改该NPC的环境声音、受伤声音、死亡声音。

### 查看当前NPC已设置的声音

查看当前NPC已设置的声音：

`/npc edit sounds list <all|ambient|hurt|death>`

如果想一次性查看该NPC的三种声音（环境声音、受伤声音、死亡声音），只需要输入以下指令：

`/npc edit sounds list all`

如果只是想查看该NPC的单种声音，例如受伤声音，那么只需要输入以下指令：

`/npc edit sounds list hurt`

### 为NPC添加单种声音

可以使用一下指令来为NPC添加单种声音，指令如下：

`/npc edit sounds add <ambient|hurt|death> <resource>`

请确保设置的声音资源路径是正确的，否则NPC将不会发出任何声音。如果遇到了错误也请不要担心，错误的声音资源路径是可逆的，可以使用正确的指令来覆盖掉错误的指令即可。

可以使用带有标签的声音来完成，会显示更加清除的路径来避免发送错误。

例如：如果想让你的NPC的声音听起来像是一只鸡的话，请输入以下指令：

`/npc edit sounds add ambient entity.chicken.ambient`

也可以加上命名空间来让路径更清楚，避免引用了错误的声音资源路径。

`/npc edit sounds add ambient minecraft:entity.chicken.ambient`


### 删除NPC的单种声音

如果想更改或者设置错了声音资源路径，那么也可以使用指令来删除NPC的单种声音或者所有声音。

删除声音的指令如下：

`/npc edit sounds remove all|<<ambient|hurt|death> all|<<index|resource> <identifier>>>`

看起来也许有点复杂，但是不如把它们分解出来讲解，使其更加可视化。

只需要稍微分解一下，就可以得到该指令的树状结构，如下图所示：

```
/npc edit sounds remove all
                      |
                      -- <ambient|hurt|death> all
                                            |
                                            -- <index|resource> <identifier>
```

在该结构里，只有竖线“`|`”是向下选择，以横线“`--`”为开头的是可选项，可以在竖线“`|`”的右侧来选择可选项中的一种选项。

还是不懂吗？那我们不如列举一些例子：

假如说想在特定的NPC中删除 __*所有*__ 类型中的 __*所有*__ 声音，那么只需要输入以下这条指令：

`/npc edit sounds remove all`

如果想在特定的声音类型中删除 __*特定*__ 类型中的 __*所有*__ 指定声音，例如：删除NPC的死亡类型中的所有声音，那么可以使用以下指令来完成该操作：

`/npc edit sounds remove death all`

考虑到另外一种情况：想从 __*特定*__ 的声音类型中删除一个 __*特定*__ 的声音，那么有两种情况：

`/npc edit sounds list all`


```
目前设置的环境声音：
1. minecraft:entity.chicken.ambient
2. minecraft:entity.villager.ambient
目前设置的受伤声音：无
目前设置的死亡声音：无
```


如果想删除村民的环境声音，那么可以使用特定的列表条目中的编号来删除声音：

`/npc edit sounds remove ambient index 2`

如果想根据名称来删除村民的环境声音：

`/npc edit sounds remove ambient resource minecraft:entity.villager.ambient`

不使用命名空间也是可以正常删除村民的环境声音：

`/npc edit sounds remove ambient resource entity.villager.ambient`

如果想根据索引来处理声音的话，需要在声音的类型中添加索引的关键字，不使用关键词将无法删除声音，从技术上来说这指的是声音资源路径，详细请参考[我的世界wiki](https://minecraft.fandom.com/wiki/Resource_location)

_Remember:_ 使用Tab键来补全指令会让操作更简单！