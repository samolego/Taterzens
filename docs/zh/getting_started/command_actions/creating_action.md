---
title: 添加指令操作
---


# 新的右键操作

---


## NPC新增指令

这个操作很简单，就跟使用以下指令一样：
```
/npc edit commands add minecraft <command>
```

*注意：当NPC添加了右键指令后，所有`同一个权限组`的指令在右键时都会**一次性**全部执行。*

所有的指令都是只会在玩家右键NPC时执行。

当玩家第二次右键时，会执行第二个权限组里的指令，以此类推，每右键一次都会执行新的权限组里的指令。
<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/ygkj7WZlhq0" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
## 我想针对与我的NPC互动的玩家！

没有比这更容易了！有了模组内置的目标选择器，只需要将指令自带的目标选择器`@a、@s`等换成`--clicker--`目标选择器即可。当玩家右键该NPC时，将会把目标选择到右键NPC的玩家。

假如有一名叫`samo_lego`的玩家想与NPC做出交互，只需要添加以下几个指令：
that we previously added the following command:
```
/npc edit commands add minecraft give --clicker-- sunflower{display:{Name:'{"text":"--clicker--'s coin"}'}}
```

NPC的执行指令会更改为以下内容：
```
/give samo_lego sunflower{display:{Name:'{"text":"samo_lego's coin"}'}} 1
```

玩家将会获得一个`向日葵”`物品，但是名字是`“samo_lego的硬币”`。
