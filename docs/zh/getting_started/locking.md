---
title: 锁定NPC
---

# 锁定NPC

---

## 默认
每个NPC在生成时都会默认锁定在特定的玩家上。*如果想禁用此操作，请在[config](../../installation/config)中修改。*
```brigadier
/taterzens config edit lockAfterCreation false
```

## 锁定有什么作用？
管理员可能需要一个`权限等级`为`4`的NPC，希望该NPC能够执行*所有*[指令](../command_actions/)管理员指令。但是也希望玩家拥有属于自己的NPC，并且能够编辑该NPC。

如果不锁定的话，那么这就意味着如果在NPC上添加了`/stop`指令，那么哪怕没有权限的玩家也能通过右键该NPC来关闭服务器。

## 解锁
如果想让NPC对每个玩家都可以编辑的话，请在选中NPC后输入以下指令：
```brigadier
/npc unlock
```

## 锁定NPC
如果想为NPC上锁，请在选中NPC后输入以下指令：
```brigadier
/npc lock
```
