---
layout: page
title: Messages
parent: Play
has_children: true
nav_order: 3
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

<video controls="true" allowfullscreen="true" width="100%">
	<source src="https://samolego.github.io/Taterzens/docs/assets/video/messages.mp4" type="video/mp4">
	<p>Your browser does not support the video element.</p>
</video>

## And then?

The NPC will send those messages to any player in range, with a certain delay.
It can be changed in config, look for the `messageDelay` option.
