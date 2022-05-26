---
title: Messages
---


# Messages

---


## Make them speak!

Taterzens are simple creatures; you enter
their message editor and they will remember things
you send in chat.

You can edit those messages later or even delete them,
if you don't like them anymore.


```
/npc edit messages list
```

will show you all the messages of selected Taterzen.

## Chat editing

### Adding new messages to Taterzen

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

### I want colored messages!

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

=== "Chat"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/que9BA9BwLs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

=== "GUI"
	<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/AAEuWPYtkcI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## And then?

The NPC will send those messages to any player in range, with a certain delay.
It can be changed in config, look for the `messageDelay` option.

## Editing messages

*Oh no, I've made a typo! How do I fix it?*

1. Check the message id by running
```
/npc edit messages list
```

<span style="color:aqua">
	Taterzen <span style="color:gold">Taterzen</span> has the following messages. Click on one to edit it.
</span>

<span style="color:gold">
	1-> Discord invite! <span style="color:red">X</span>
</span>

<span style="color:DarkGoldenRod">2-> Second message.</span>	<span style="color:red">X</span>

<span style="color:gold">
	3-> Yet another message ... <span style="color:red">X</span>
</span>


2. Click on the message you'd like to edit or run
```
/npc edit messages <id>
```

3. Type the new message in chat and send it.


## Deleting messages

Simply run
```
/npc edit messages <id> delete
```
to delete the message with id `<id>`.
Or click the <span style="color:red">X</span> next to the message when listing them.

To clear **all** the messages, run
```
/npc edit messages clear
```
