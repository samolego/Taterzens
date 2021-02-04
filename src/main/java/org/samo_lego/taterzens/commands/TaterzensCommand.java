package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class TaterzensCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // Ignore this for now, we will explain it next.
        dispatcher.register(CommandManager.literal("taterzens")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .then(CommandManager.argument("config", word())
                        .then(CommandManager.literal("reload")
                                .executes(TaterzensCommand::reloadConfig)
                        )
                )
        );
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        return 1;
    }
}
