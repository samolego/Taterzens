package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;

public class ProfessionCommand {
    public static final LiteralCommandNode<CommandSourceStack> PROFESSION_COMMAND_NODE;

    /**
     * Registers "/profession" node. Can be used to manage scarpet professions,
     * or other professions if they hook into the node.
     * @param dispatcher command dispatcher
     * @param dedicated whether server is dedicated or singleplayer.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        dispatcher.getRoot().addChild(PROFESSION_COMMAND_NODE);
    }

    static {
        PROFESSION_COMMAND_NODE = literal("profession")
                    .requires(src -> permissions$checkPermission(src, "taterzens.profession", config.perms.traitCommandPermissionLevel)
                )
                .build();
    }
}
