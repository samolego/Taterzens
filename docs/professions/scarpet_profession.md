---
title: Scarpet Profession
---


# Hello scarpet world?

---

???+ note
    This is curently only available on Fabric.

???+ note
    You'll need to install [CarpetMod](https://github.com/gnembon/fabric-carpet/releases/latest).

# Script profession installation

1. Download the script professions you want and put them in `minecraft-folder/world/scripts`.
2. Run the server / singleplayer.
3. Make sure the script is loaded (`/script load <script name>`). (To autoload the scripts, use `/carpet setDefault scriptsAutoload true`)
4. Give taterzen the `taterzens:scarpet_profession` (`/npc edit professions add taterzens:scarpet_profession`).

## Creating a merchant

Download the [merchant script](https://github.com/samolego/TaterzenScarpetProfessions/blob/master/scripts/merchant.sc)
and load it using the steps described above.

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/7kT-GflSREc" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

*Now you can trade:*

* `16 oak_log(s)` for `3 emerald(s)`
* `32 stone` for `2 gold`.

## Modifying scripts

Do you want to add trades to the above example?
Simply add new items in the [global_item_map](https://github.com/samolego/TaterzenScarpetProfessions/blob/e4d5888b321bb9a3d4a6e130c79a5e185dea8a8d/scripts/merchant.sc#L20-L34).
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


Also check out **[scarpet wiki](https://github.com/gnembon/fabric-carpet/wiki/Scarpet)**
and **[docs](https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/Documentation.md)**
for help with custom script creation.