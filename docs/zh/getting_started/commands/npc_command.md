---
title: NPC指令
---

# `NPC` 指令

---

## 这些指令的作用是什么？

更好的帮助你管理你名下的NPC。

在编辑NPC之前请确保已经选择了一名NPC，指令是：/npc select

### 生成
`/npc create <NPC名称>` - 生成一名NPC，同时默认选择该NPC。

### 删除
`/npc remove` - 删除**已选择**的NPC。

### 传送
`/npc tp <目的地>` - 将指定的NPC传送到指定的坐标。
* `<目的地>` 目的地可以是指定实体，也可以是指定坐标。

### NPC列表
`/npc list` - 列出所有名下已加载的NPC。

### Selecting
可以通过NPC的ID、名称、UUID或者面前的方式来选择NPC。

注意：`ID`指的是输入`/npc list`指令时的NPC列表的对应NPC左侧的数字，例如：1、2、3、4、5这种的数字ID。`UUID`是我的世界默认区分生物的`唯一识别码`，有关UUID的内容，请查看[我的世界Wiki](https://minecraft.fandom.com/zh/wiki/%E9%80%9A%E7%94%A8%E5%94%AF%E4%B8%80%E8%AF%86%E5%88%AB%E7%A0%81)。

#### **Id**
1. 输入`/npc list`指令可查看目前已加载区块中的NPC的ID。
2. 输入`/npc select id <ID>`指令来选择对应ID的NPC。

#### **UUID**
1. 输入`/npc list`指令可查看目前已加载区块中的NPC。
2. 在聊天栏中使用鼠标指向NPC的右侧的`(UUID)`栏即可查看该NPC的UUID。
3. 点击列表中的`(UUID)`文本。
4. 使用“Ctrl+a”和“Ctrl+c”组合快捷键将NPC的UUID复制到电脑的剪切板中。
5. 输入`/npc select uuid <UUID>`指令后使用“Ctrl+v”快捷键将刚刚复制到的`<UUID>`粘贴到刚刚复制的`<UUID>`栏中。
6. 一切顺利的话那么将会根据UUID来选择该NPC。

#### **名称**
1. 输入`/npc list`指令可查看目前已加载区块中的NPC。
2. 输入`/npc select name <名称>`指令，根据NPC列表中的NPC名称来选择该NPC。

注意：如果你的NPC名称里包含空格（例如：史 蒂 夫），请确保使用名称来选择NPC时将NPC的名称使用引号全部圈起来了。在该例子中使用的指令是`/npc select name "史 蒂 夫"`，如果没有引号，那么NPC将不会被选中。

注意：如果在附近存在多个重名的NPC，那么会选择失败然后弹出一个错误信息。

#### **近距离**
1. 面向要选择的NPC。
2. 输入`/npc select`指令即可选择该NPC。

### 编辑

你可以更改NPC的多种功能，例如：实体类型、皮肤、姿势、名称、聊天信息、右键动作等。
