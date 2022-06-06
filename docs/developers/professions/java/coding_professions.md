---
title: Coding Profession
---


# Hello world?

---

Whether you are an experienced programmer or a beginner,
you should (hopefully) be able to understand how to create
a simple profession.

A sample repository with the profession is
available on [GitHub](https://www.github.com/samolego/TaterzenProfessionExampleMod).

## Creating your own profession class
First off, create a class implementing
[`TaterzenProfession`](https://samolego.github.io/Taterzens/dokka/latest-snapshot/common/common/org.samo_lego.taterzens.api.professions/-taterzen-profession/index.html)
interface (found in `org.samo_lego.taterzens.api.professions`).

Create a unique ResourceLocation for your profession as well, as we will
need it later.


```java
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class MyFirstProfession implements TaterzenProfession {
	public static final ResourceLocation ID = new ResourceLocation("your_mod_id", "my_profession");
}
```

You will then need to implement the methods.
If you don't want to change certain behaviours, just leave those empty.
If you don't know what certain methods mean, you can always check out
[code docs](https://samolego.github.io/Taterzens/dokka/latest-snapshot/common/common/org.samo_lego.taterzens.api.professions/-taterzen-profession/index.html).

For methods using `InteractionResult`, your return variable can mean the following:

* `InteractionResult#PASS` - Default; continues ticking other professions.
* `InteractionResult#CONSUME` - Stops processing others, but continues with base Taterzen movement tick.
* `InteractionResult#FAIL` - Stops whole movement tick.
* `InteractionResult#SUCCESS` - Continues with super.tickMovement(), but skips Taterzen's movement tick.

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

    /**
     * Method used for creating the new profession for given taterzen.
     *
     * @param taterzen taterzen to create profession for
     * @return new profession object of taterzen.
     */
    @Override
    public TaterzenProfession create(TaterzenNPC taterzen) {
        MyFirstProfession profession = new MyFirstProfession();
		
        // Assigning the Taterzen to the profession so we can use it later.
        profession.npc = taterzen;
        return profession;
    }
}
```

## Registering profession
The last step you have to do is to register all your profession types.
There's a method in TaterzensAPI class that you can use.

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
