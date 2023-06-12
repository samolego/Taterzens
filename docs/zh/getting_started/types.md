---
title: 实体类型
---


# 实体类型

---


## 更改实体类型

**要想执行此操作，需要安装“[DisguiseLib](https://modrinth.com/mod/disguiselib/versions)”模组。**

实体类型理论来讲支持所有原版的实体，除了部分特殊的实体，例如：船、恶魂的火球、被激活的TNT、末地水晶等等。
理论来讲，其他模组的实体*应该*也支持。

想要更改实体类型，只需要在选中NPC后输入以下指令：
```
/npc edit type <实体类型>
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/OD84_yOOUoA" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## 添加自定义NBT

DisguiseLib模组支持使用nbt来为生物进行伪装，要想使用伪装，请在实体类型右侧输入nbt数据：
```
/npc edit type <实体类型> <nbt>
```

### 示例
???+ hint "获得自定义NBT"
	前往[mcstacker](https://mcstacker.net/)上然后选择`summon`类型。设置实体并且添加自己想要的NBT之后，将`{}`之间的内容复制，包括括号，最后将复制的内容添加到上述指令中实体类型右侧的`<nbt>`栏中。

#### 沙漠村民
```
/npc edit type minecraft:villager {VillagerData:{profession:"minecraft:armorer",type:"minecraft:desert"}}
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/jrYyGCQgB-o" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

#### 小鸡
```
/npc edit type minecraft:chicken {Age:-1}
```

#### 自定义盔甲架
???- hint "轻松地自定义盔甲架"
	玩家Patbox做了一个很酷的盔甲架编辑器，同时也支持Disguise里的实体，更多信息请查看[盔甲架编辑器](https://www.curseforge.com/minecraft/mc-mods/armor-stand-editor)页面。

```
/npc edit type minecraft:armor_stand {HasVisualFire:1b,Glowing:0b,ShowArms:1b,Small:1b,NoBasePlate:1b}
```