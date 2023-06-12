---
title: 信息
---


# 信息

---


## 允许NPC说话！

NPC是一种简单的生物，当你进入NPC的信息编辑模式时，在聊天栏发送的所有信息NPC都可以记住且保存它们。

你可以随时查看这些信息，如果不喜欢某条信息也可以点击信息右侧的`X`来删除。

输入以下指令可以查看目前已选择的NPC会发送的所有信息：
```
/npc edit messages list
```

## 聊天编辑

### 向NPC添加信息

选中NPC后输入以下指令：
```
/npc edit messages
```

会通过聊天栏告诉你以下信息：

<span style="color:magenta">
	**你已进入<span style="color:cyan"><NPC名称></span>的信息编辑界面，请输入相同的指令退出编辑。**
</span>

<span style="color:chartreuse">
	此时在聊天栏中发送信息，<span style="color:cyan"><NPC名称></span>将会重复发送当前已设置的信息。你可以发送普通的文本，也可以使用基于/tellraw指令的JSON颜色文本。
</span>

此时在聊天栏中发送的任何信息都会被添加到NPC的`即将发送信息`的列表中。

### 设置彩色信息！

NPC的信息列表可以设置带有彩色文本的信息！
同时也支持[tellraw的json文本](https://minecraft.fandom.com/zh/wiki/%E5%91%BD%E4%BB%A4/tellraw)格式。

1. 使用文本生成tellraw指令！
有一些工具可以制作此功能，推荐使用[MCStacker](https://mcstacker.net/)。
例如：
```
/tellraw @p {"text":"Discord的邀请！","color":"gold","bold":true,"italic":true,"clickEvent":{"action":"open_url","value":"https://discord.gg/9PAesuHFnp"}}
```

将会发送一个以下类似的文本：

<span style="color:gold">
	Discord的邀请！
</span>

2. 将所有文本从第一个`{`开始复制到最后。
```
{"text":"Discord的邀请！","color":"gold","bold":true,"italic":true,"clickEvent":{"action":"open_url","value":"https://discord.gg/9PAesuHFnp"}}
```
3. 最后在聊天栏中发送该信息，NPC会解析该内容！

=== "聊天"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/que9BA9BwLs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

=== "GUI"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/AAEuWPYtkcI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## 然后呢？

当一切设置完毕后，NPC会尝试在NPC附近一定范围内的任何玩家发送该信息，信息拥有一定的延时，可以在配置文件中更改，寻找有关`messageDelay`的选项即可。

## 编辑信息

*哦不，我不小心打错字了！我应该如何才能修改它呢？*

1. 可以输入以下指令来查看信息的ID：
```
/npc edit messages list
```

<span style="color:aqua">
	NPC<span style="color:gold"><NPC名称></span>会在聊天栏中发送以下信息：
</span>

<span style="color:gold">
	1-> Discord的邀请！ <span style="color:red">X</span>
</span>

<span style="color:DarkGoldenRod">2-> 第二条信息。</span>	<span style="color:red">X</span>

<span style="color:gold">
	3-> 第三条信息... <span style="color:red">X</span>
</span>


2. 点击想要编辑信息后输入以下指令：
```
/npc edit messages <id>
```

3. 在聊天栏中输入新的信息后重新发送即可完成修改。


## 删除信息

在聊天栏中输入以下指令：
```
/npc edit messages <id> delete
```
输入上述指令后会删除`<id>`内的信息，如果不想输入指令也可以在聊天栏中点击信息旁边的<span style="color:red">X</span> 来删除。

如果想删除该NPC的**所有**信息，请在聊天栏中输入以下指令：
```
/npc edit messages clear
```
