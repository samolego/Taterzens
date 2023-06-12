---
title: 地毯职业
---


# 你好地毯世界？

---

???+ 注意
    该功能仅支持Fabric加载器。

???+ 注意
    你需要安装[CarpetMod](https://github.com/gnembon/fabric-carpet/releases/latest)模组才能使用该功能。

# 安装脚本职业

1. 下载所需要的职业脚本，然后将它们安装在`minecraft-folder/world/scripts`。
2. 启动服务器或者单人游戏。
3. 确保脚本已经加载（`/script load <脚本名称>`）。（加载脚本请使用指令：`/carpet setDefault scriptsAutoload true`）
4. 给予NPC`taterzens:scarpet_profession`（`/npc edit professions add taterzens:scarpet_profession`）。

## 新建商人

下载[商人脚本](https://github.com/samolego/TaterzenScarpetProfessions/blob/master/scripts/merchant.sc)后根据上面的安装步骤使用它。

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/7kT-GflSREc" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

*现在可以与该NPC交易：*

* `16个橡木原木` -> `3个绿宝石`
* `32个石头` -> `2个金锭`

## 修改脚本

你想在上面的实力中添加自己的交易吗？只需要在[global_item_map](https://github.com/samolego/TaterzenScarpetProfessions/blob/e4d5888b321bb9a3d4a6e130c79a5e185dea8a8d/scripts/merchant.sc#L20-L34)里添加新的物品即可。
```diff
global_item_map = {
    // Takes 32 stone and gives 2 gold
    'stone' -> {
        'take_count'-> 32,
        'trade_item'-> 'gold',
        'trade_count'-> 2
    },
    // Takes 16 oak_log(s) and gives 3 emerald(s)
    'oak_log' -> {
        'take_count'-> 16,
        'trade_item'-> 'emerald',
        'trade_count'-> 3
-    }
+    },
+    // Takes 1 dirt and gives 64 diamond. Stonks!
+    'dirt' -> {
+        'take_count'-> 1,
+        'trade_item'-> 'diamond',
+        'trade_count'-> 64
    }
};
```


你也可以查看**[地毯wiki](https://github.com/gnembon/fabric-carpet/wiki/Scarpet)** 和 **[文档](https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/Documentation.md)**来获得自定义脚本的相关指南。