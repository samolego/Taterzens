---
layout: page
title: Editing messages
parent: Messages
grand_parent: Play
---


# Editing messages

---


## Oh no, I've made a typo! How do I fix it?

1. Check the message id by running
```
/npc edit messages list
```

<span style="color:aqua">
	Taterzen <span style="color:yellow">Taterzen</span> has the following messages. Click on one to edit it.
</span>

<span style="color:gold">
	1-> Discord invite! <span style="color:red">X</span>
</span>

<span style="color:yellow">2-> Second message.</span>	<span style="color:red">X</span>

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