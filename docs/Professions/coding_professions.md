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
[`TaterzenProfession`](https://samolego.github.io/Taterzens/dokka/common/common/org.samo_lego.taterzens.api.professions/-taterzen-profession/index.html)
interface (found in `org.samo_lego.taterzens.api.professions`).

Create a unique identifier for your profession as well, as we will
need it later.


```java
import org.samo_lego.taterzens.npc.TaterzenNPC;

public class MyFirstProfession implements TaterzenProfession {
	public static final Identifier ID = new Identifier("your_mod_id", "my_profession");
}
```

You will then need to implement the methods.
If you don't want to change certain behaviours, just leave those empty.
If you don't know what certain methods mean, you can always check out
[code docs](https://samolego.github.io/Taterzens/dokka/common/common/org.samo_lego.taterzens.api.professions/-taterzen-profession/index.html).

For methods using `ActionResult`, your return variable can mean the following:
* `ActionResult#PASS` - Default; continues ticking other professions.
* `ActionResult#CONSUME` - Stops processing others, but continues with base Taterzen movement tick.
* `ActionResult#FAIL` - Stops whole movement tick.
* `ActionResult#SUCCESS` - Continues with super.tickMovement(), but skips Taterzen's movement tick.

The **base** method you need to implement is `create(TaterzenNPC)`.
This should create a new profession object for the passed Taterzen
and return it. See the below example.

```java
public class MyFirstProfession implements TaterzenProfession {
    public static final Identifier ID = new Identifier(MODID, "my_profession");
    private TaterzenNPC npc;

    /**
    * Deafult constructor. Will be used to register our profession later.
    * A profession created with this constructor isn't really useful, as it doesn't
    * have a Taterzen assigned to it.
    * That's why we need another {@link MyFirstProfession#create(TaterzenNPC)}.
    */
    public MyFirstProfession() {
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d pos, Hand hand) {
        player.sendMessage(new LiteralText("You have interacted with ").append(this.npc.getName()), false);
        return ActionResult.PASS;
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

    public static final String MODID = "taterzens";

    /**
	* Put this in initialisation method of your mod.
	* Those are different if you're using forge / fabric.
    */
    public static void onInitialize() {
        // Profession registering
        TaterzensAPI.registerProfession(MyFirstProfession.ID, new MyFirstProfession());
    }
}

```
