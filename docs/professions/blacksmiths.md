---
title: Blacksmiths
---

A Taterzen addon that brings tool-repairing-guys to Minecraft.

[![Stars](https://img.shields.io/github/stars/samolego/TraderNPCs?style=flat-square)](https://github.com/samolego/Blacksmiths)

???+ error "Too quick you are"
	Not released yet. See [source](https://github.com/samolego/Blacksmiths) for more info.

# Traders

TraderNPCs addon allows you to create Taterzens which can repair tools.
They can be configured how much durability per second they repair, whether they "work" in unloaded chunks, etc.

## Usage

Make sure to [assign](./assigning_professions.md#giving-taterzen-a-profession) the `blacksmiths:blacksmith` profession to your Taterzen.
After that, simply use right click on the Taterzen to open the GUI.

## Wait ... unloaded chunks?

Yes, you've read it right. As Blackmiths operate via system time, they are not
dependant on the server tick rate. What's more, your tools can be repaired even
when your pc is off :smile:. You can toggle the unloaded-chunl-working per-taterzen as well.
