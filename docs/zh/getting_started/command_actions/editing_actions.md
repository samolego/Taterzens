---
title: 编辑动作
---


# 编辑动作

---


## 哦不，我不喜欢再有这条指令了！

1. 使用以下指令来查看NPC目前的指令列表：
```
/npc edit commands list
```


<span style="color:aqua">
	NPC<span style="color:yellow"><NPC名称></span>将在玩家右键时执行以下指令：
</span>

<span style="color:gold">
	1-> give @r diamond <span style="color:red">X</span>
</span>

<span style="color:yellow">2-> tp -\-clicker-\- ~ ~10 ~</span>	<span style="color:red">X</span>

<span style="color:gold">
	3-> msg @a I'm broke now! <span style="color:red">X</span>
</span>


选择想要删除的指令，然后查看指令的左边这个数字ID，然后在聊天栏中点击需要删除的指令或者输入以下指令：
```
/npc edit commands group id <指令组id> remove <指令id>
```


## 删除所有指令
如果想删除NPC添加的**所有**指令，需要输入以下指令：
```
/npc edit commands clear
```