package org.samo_lego.taterzens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.samo_lego.taterzens.common.Taterzens;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.common.Taterzens.config;

public class ProfessionCommand {
    public static final LiteralCommandNode<CommandSourceStack> PROFESSION_COMMAND_NODE;

    /**
     * Registers "/profession" node. Can be used to manage scarpet professions,
     * or other professions if they hook into the node.
     * @param dispatcher command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.getRoot().addChild(PROFESSION_COMMAND_NODE);
    }

    static {
        PROFESSION_COMMAND_NODE = literal("profession")
                    .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.profession", config.perms.professionCommandPL)
                )
                .build();
    }
}
