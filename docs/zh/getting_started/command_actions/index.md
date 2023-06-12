---
title: 右键操作
---


# 指令动作

---


## 右键点击动作

从`0.3.0`版本开始，NPC可在右键时执行多条指令。

可以是攻击的指令，给予物品的指令，也可以是针对NPC本身、随机玩家、所有玩家的指令等等。

NPC甚至可以通过执行指令来编辑自身！更多信息请查看下方视频中的例子。

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/mxVgZmFcFPA" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

如果想查看NPC在右键时会执行的指令，请输入以下指令查看：

```
/npc edit commands list
```


## 权限等级

(啊？*为什么我的指令不生效？*)

新生成的NPC权限等级可以在[config](../../installation/config.md)文件中查看。

如果想设置一名NPC执行非管理员指令，可以输入以下指令来设置指定NPC的权限等级：
```
/npc edit commands setPermissionLevel 0
```

如果想让该NPC执行`op`指令，请将该NPC的权限等级设置为`4`，指令如下：
```
/npc edit commands setPermissionLevel 4
```

## 点击选择器

在大多数情况下，你可能只是想让NPC执行右键操作，正常情况下你可以使用原版的`@p`选择器，而`@p`选择的是最近玩家，而有时最近的玩家可能并不是想与NPC互动的玩家，**如果使用`@s`选择器，而生效实体只会是NPC本身而不是玩家**。

你可以使用`--clicker--`来替换，本模组在执行指令时会将任何出现的信息替换为执行命令时的玩家名称。

<br>

给予点击的玩家一个金锭：
```
/npc edit commands add minecraft give --clicker-- gold_ingot
```


让点击的玩家向上传送10格：
```
/npc edit commands add minecraft execute at --clicker-- run tp --clicker-- ~ ~10 ~
```

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/PYkcRGhlwWw" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
