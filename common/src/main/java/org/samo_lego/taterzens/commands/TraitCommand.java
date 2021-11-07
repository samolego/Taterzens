package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.CARPETMOD_LOADED;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;

public class TraitCommand {
    public static LiteralCommandNode<CommandSourceStack> TRAIT_COMMAND_NODE;

    /**
     * Registers "/trait" node. Can be used to manage scarpet professions.
     * @param dispatcher command dispatcher
     * @param dedicated whether server is dedicated or singleplayer.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        TRAIT_COMMAND_NODE = dispatcher.register(literal("trait")
                .requires(src -> CARPETMOD_LOADED && permissions$checkPermission(src, "taterzens.trait", config.perms.traitCommandPermissionLevel))
        );
    }
}
