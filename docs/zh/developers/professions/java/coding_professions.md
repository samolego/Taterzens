---
title: 职业编程
---


# 你好世界？

---

**您好，我是制作该中文文档的玩家Annijang，由于我并没有学过java，所以我对编程几乎一窍不通，如果你正好查看了该文档，请帮助我解决问题，因为我是直译该文档，我并不是很清楚java的运行逻辑和专业用词，如果你发现下面的文档有任何的描述问题，请在我的mcbbs论坛上联系我，我在论坛的名称为Annijang，UID是2169201，谢谢您。**

无论你是编程的新人还是老油条，你应该都希望想为NPC来制作一个简单的职业。

在[GitHub](https://www.github.com/samolego/TaterzenProfessionExampleMod)上有一个与此相关的实力库可以帮助你了解。

## 制作自己的职业
首先，需要创建一个[`TaterzenProfession`](https://samolego.github.io/Taterzens/dokka/latest-snapshot/common/common/org.samo_lego.taterzens.api.professions/-taterzen-profession/index.html)接口中的类（在`org.samo_lego.taterzens.api.professions`）。

为职业新建一个ResourceLocation，因为以后会需要使用它。

```java
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class MyFirstProfession implements TaterzenProfession {
	public static final ResourceLocation ID = new ResourceLocation("your_mod_id", "my_profession");
}
```
然后需要实现此类方法，如果不想更改某些行为的话请将行为留空。如果你不知道什么是行文，你可以随时查看[代码文档](https://samolego.github.io/Taterzens/dokka/latest-snapshot/common/common/org.samo_lego.taterzens.api.professions/-taterzen-profession/index.html)。

对于使用`InteractionResult`的方法，你的返回变量可以是以下内容：

* `InteractionResult#PASS` - 默认，选择其他职业。
* `InteractionResult#CONSUME` - 停止处理，继续使用基础的NPC方式。
* `InteractionResult#FAIL` - 取消所有运动方式。
* `InteractionResult#SUCCESS` - 继续使用super.tickMovement()，但跳过NPC的方法。

```java
public class MyFirstProfession implements TaterzenProfession {
    public static final ResourceLocation ID = new ResourceLocation(MODID, "my_profession");
    private final TaterzenNPC npc;

    public MyFirstProfession(TaterzenNPC npc) {
        this.npc = npc;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3d pos, Hand hand) {
        player.sendMessage(Component.literal("You have interacted with ").append(this.npc.getName()), false);
        return InteractionResult.PASS;
    }
}
```

## 注册职业
最后需要为职业选择一个类型，在TaterzensAPI的类中有一个方法，你可以使用。

```java
import org.samo_lego.taterzens.api.TaterzensAPI;
import my.custom.package.MyFirstProfession;

public class MyProfessionsMod {

    public static final String MODID = "my_professions_mod";

    /**
	* Put this in initialisation method of your mod.
	* Those are different if you're using forge / fabric.
    */
    public static void onInitialize() {
        // Profession registering
        TaterzensAPI.registerProfession(MyFirstProfession.ID, MyFirstProfession::new);
    }
}

```
