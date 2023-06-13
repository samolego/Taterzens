---
title: 铁匠
---

为游戏内添加了一个铁匠职业来修复玩家的工具。

[![CurseForge](https://cf.way2muchnoise.eu/versions/For%20MC_550916_all.svg?style=flat-square)](https://www.curseforge.com/minecraft/mc-mods/blacksmiths)
[![Stars](https://img.shields.io/github/stars/samolego/Blacksmiths?style=flat-square)](https://github.com/samolego/Blacksmiths)

# 铁匠

铁匠模组允许你给予NPC一个铁匠的职业，铁匠可以修复武器装备的耐久，也可以设置每次消修复需要消耗多少金额或者每次修复的耐久等。

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/RrduZUPcmfY" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## 使用方式

请确保你将`blacksmiths:blacksmith`铁匠职业[分配](assigning_professions.md#giving-taterzen-a-profession)给了NPC，然后只需要右键铁匠即可打开铁匠的GUI界面。

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/9gJV5l_lSlI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## 定价

如果你正好安装了[Grand Economy](https://www.curseforge.com/minecraft/mc-mods/grand-economy)模组，那么你可以使用该模组来设置每次修复需要的价格，否则你可以设置成物品和它的“worth”。

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/L8c5hZvJOBU" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## NPC耐久修复设置

你想让玩家拥有不同的修复耐久数量的NPC吗？那么请使用以下指令：

```
/profession blacksmith <规则>
```

你可以使用以下规则：

* `costPerDurabilityPoint` - 设置修复1点耐久消耗的金额数量
* `durabilityPerSecond` - 每秒可以修复多少耐久，数字可以小于1.
* `workInUnloadedChunks` - NPC是否可以在未加载的区块里工作。

## 等等...未加载的区块？

是的，你没有看错，由于每个服务器的延迟都不一样，而本模组使用了特殊的技术允许NPC在未加载的区块里持续修复工具，只要服务器一直开启的话那么工具可以始终进行修复:smile:。
