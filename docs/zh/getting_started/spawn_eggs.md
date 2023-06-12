---
title: 刷怪蛋
---

# 刷怪蛋

---

## 代替命令刷怪

NPC也可以通过指令来生成，最主要的是也可以将预设与刷怪笼互相搭配！

???+ note
    如果是通过刷怪笼生成的NPC，NPC默认不会被选中。

### 基础生成
```brigadier
/give @s chicken_spawn_egg{EntityTag:{id:"taterzens:npc"}} 1
```

### 根据预设生成

假如我们有一个名称为my_npc的NPC预设文件，使用下述指令可以获得一个包含该NPC的[预设NPC](../presets)刷怪蛋。
```brigadier
/give @s chicken_spawn_egg{EntityTag:{id:"taterzens:npc", PresetOverride:"my_npc"}} 1
```
如果想要使用其他NPC的预设，只需要编辑上述指令中的`PresetOverride`更换为其他的预设NPC文件名即可。
