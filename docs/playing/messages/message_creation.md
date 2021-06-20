---
layout: page
title: Creating messages
parent: Messages
grand_parent: Play
---


# Creating new messages

---


## Adding new messages to Taterzen

Running
```
/npc edit messages
```

will send you this:

<span style="color:magenta">
	**You've entered message editor for <span style="color:cyan">Taterzen</span>.**
	**Enter same command to exit.**
</span>

<span style="color:chartreuse">
	Send messages in chat and <span style="color:cyan">Taterzen</span> will repeat them.
	You can use normal text or tellraw structure (for colors).
</span>

That means that any messages you send to chat will be redirected to
Taterzen's `to-say` list instead.

## I want colored messages!

No problem, Taterzens mod has you covered!
It supports [tellraw json text format](https://minecraft.gamepedia.com/Commands/tellraw) as well.

1. Generate any tellraw command with the text you want.
There are online tools that can do it, I recommend using [MCStacker](https://mcstacker.net/).
```
/tellraw @p {"text":"Discord invite!","color":"gold","bold":true,"italic":true,"clickEvent":{"action":"open_url","value":"https://discord.gg/9PAesuHFnp"}}
```

This would send a following-like text:
<span style="color:gold">
	Discord invite!
</span>

2. Copy all text from first `{` all the way to the end.
```
{"text":"Discord invite!","color":"gold","bold":true,"italic":true,"clickEvent":{"action":"open_url","value":"https://discord.gg/9PAesuHFnp"}}
```
3. Send it in chat and NPC will parse it.
