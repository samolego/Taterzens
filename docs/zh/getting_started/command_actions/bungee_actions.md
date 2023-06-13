---
title: 跨服操作
---

# 添加代理操作

---

## 设置

1. 请确保在[config](../../installation/config.md)文件中将`"bungee" -> "enable_commands"`设置为`true`。

   (`/taterzens config edit bungee enableCommands true`)

2. 也可以使用以下方式来添加一个bungee/velocity指令。
```
/npc edit commands add bungee <cmd> <player> <parameter>
```
说明
* `cmd` - bungee的指令，见下文。
* `player` - 执行操作的玩家
* `parameter` - 参数，如果有指令需求的话。

## 可用指令
* `server` - 将玩家传送到另外一个服务器上。（参数：服务器名称）
* `message` - 向玩家发送的信息。
* `message_raw` - 以[tellraw](https://minecraft.fandom.com/wiki/Raw_JSON_text_format)格式向玩家发送信息。
* `kick` - 踢出玩家（参数：踢出原因）

## 服务器切换

<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/hntv-TevhNs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>>
