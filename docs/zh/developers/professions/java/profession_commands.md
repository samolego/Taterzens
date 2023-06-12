---
title: 职业指令
---


# 拓展职业能力

---

一旦你成功注册了自己的职业，就应该继续前进！你可以希望这个职业可以使用指令来修改，如果你之前使用过原版的Brigadier，那么这个操作对你来讲应该也不难。

假设我们创建一个拥有**交易**的职业，我们想修改我们职业中的交易物品。

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

请确保将指令也记起来了。
