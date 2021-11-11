package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;

public class ProfessionCommand {
    public static LiteralCommandNode<CommandSourceStack> PROFESSION_COMMAND_NODE;

    /**
     * Registers "/trait" node. Can be used to manage scarpet professions.
     * @param dispatcher command dispatcher
     * @param dedicated whether server is dedicated or singleplayer.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        PROFESSION_COMMAND_NODE = dispatcher.register(literal("profession")
                .requires(src -> permissions$checkPermission(src, "taterzens.profession", config.perms.traitCommandPermissionLevel))
        );
    }
}
