---
title: 移动
---


# 移动

---


## 寻路NPC

因为本模组拓展了原版的`PathfinderMob`，所以本模组的NPC将可以设置成自动寻路。

目前NPC的寻路方式分为以下三种：

* [自由模式](#free-movement)（四处游荡，随机行走）
* [跟随模式](#strict-movement)（不停地跟随和观察玩家）
* [指定模式](#follow-movement)（自定义设置寻路路径）


如果你不希望NPC可以行走的话，请输入以下指令来禁用：
`/npc edit movement NONE` （默认）


## 跟随模式

### 观察玩家

NPC会看着4格内的一个玩家：
```
/npc edit movement FORCED_LOOK
```

=== "GUI"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/EofYSR-D4PI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

=== "指令"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/X9z0ykvXUUE" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

2. 始终遵循在[路径](path.md)里设置的路径，不会停下来休息：
```
/npc edit movement FORCED_PATH
```


## 跟随模式

1. 设置跟随类型：
```
/npc edit movement FOLLOW <跟随类型>
```

2. 设置跟随类型为UUID：
```
/npc edit movement FOLLOW UUID <需要跟随目标的UUID>
```

3. 示例

① 跟随生物：`/npc edit movement follow MOBS `

② 禁用跟随模式：`/npc edit movement follow NONE`

③ 跟随玩家：`/npc edit movement follow PLAYERS <玩家ID>`

④ 跟随指定UUID的生物：`/npc edit movement follow UUID <指定生物的UUID>`


## 自由模式

1. 如果你已经为NPC设置一个自由活动的区域，可以使用以下指令：
```
/npc edit movement FREE
```
当输入该指令后，NPC将会自由活动，没有任何限制。因此，也有可能会找不到该NPC，务必将活动区域圈起来！

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/Mv3TnTVJ2aM" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

2. 当在[路径](path.md)里设置完成路径后，如果想让NPC在行走时也可以*长时间*休息且观望四周的话，请输入以下指令：
```
/npc edit movement PATH
```