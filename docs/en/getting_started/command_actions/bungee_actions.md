---
title: Bungee Actions
---

# Adding proxy actions

---

## Setup

1. Make sure that `"bungee" -> "enable_commands"` is set to `true` in [config](../../installation/config.md).

   (`/taterzens config edit bungee enableCommands true`)

2. You can now add a bungee / velocity command via
```
/npc edit commands add bungee <cmd> <player> <parameter>
```
where
* `cmd` - bungee action command, see below
* `player` - player to execute action for
* `parameter` - parameter if required by command

## Available commands
* `server` - transfers player to another server (parameter: server name)
* `message` - sends message to the player
* `message_raw` - sends message to the player in [tellraw](https://minecraft.fandom.com/wiki/Raw_JSON_text_format) format
* `kick` - kicks player (parameter: reason for kick)

## Server redirect

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/hntv-TevhNs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>>
