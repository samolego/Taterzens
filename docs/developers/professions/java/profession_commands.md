---
title: Profession Commands
---


# Expanding profession abilities

---

Once you have made your own profession, it's time to move on!
You'd probably want it to be modifiable with commands. If you've
worked with Mojang's Brigadier before, this shouldn't be a problem.


Let's assume that we have created **trader** profession. We want to
modify the payment item of our profession.

**TraderCommand.java**
```java
public class TraderCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
		dispatcher.register(literal("trader")
			.then(literal("changeCurrency")
				.then(argument("currency item", ItemStackArgumentType.itemStack())
					.executes(TraderCommand::changeCurrency)
				)	
			)
		);
	}
	
	private static int changeCurrency(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		// Getting the selected Taterzen
		ServerPlayer player = context.getSource().getPlayer();
		TaterzenNPC npc = ((TaterzenEditor) player).getNpc();
		if(npc != null) {
			// Getting the profession if NPC has it
			TraderProfession profession = npc.getProfession(TraderProfession.ID);
			if(profession != null) {
				ItemStack stack = ItemStackArgumentType.getItemStackArgument(commandContext, "currency item");
				profession.editCurrency(stack);
				return 1;
			}
			// Otherwise send error
			player.sendMessage(Component.literal("This npc doesn't have trader profession :'( ..."), false);
		} else
			player.sendMessage(noSelectedTaterzenError(), false);
		return 0;
	}
}

```

Make sure to register your command as well.
