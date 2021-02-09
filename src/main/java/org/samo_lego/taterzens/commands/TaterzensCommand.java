package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.storage.TaterLang;

import java.io.File;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;

public class TaterzensCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // Ignore this for now, we will explain it next.
        dispatcher.register(literal("taterzens")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .then(literal("config")
                        .then(literal("reload")
                                .executes(TaterzensCommand::reloadConfig)
                        )
                )
                .then(literal("list")
                    .executes(TaterzensCommand::listTaterzens)
                )
        );
    }

    private static int listTaterzens(CommandContext<ServerCommandSource> context) {
        MutableText response = new LiteralText(lang.availableTaterzens).formatted(Formatting.AQUA);
        for(int i = 0; i < TATERZEN_NPCS.size(); ++i) {
            response.append(new LiteralText("\n" + (i + 1) + "-> ")
                    .append(TATERZEN_NPCS.get(i).getCustomName())
                    .formatted(i % 2 == 0 ? Formatting.YELLOW : Formatting.GOLD));
        }
        context.getSource().sendFeedback(response, false);
        return 0;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        File taterDir = new File(FabricLoader.getInstance().getConfigDir() + "/Taterzens");

        config = TaterConfig.loadConfigFile(new File(taterDir + "/config.json"));
        lang = TaterLang.loadLanguageFile(new File(taterDir + "/" + config.language + ".json"));

        context.getSource().sendFeedback(
                new LiteralText(lang.success.configReloaded).formatted(Formatting.GREEN),
                false
        );
        return 0;
    }
}
