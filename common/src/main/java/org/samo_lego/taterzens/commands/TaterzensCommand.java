package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.storage.TaterLang;

import java.io.File;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class TaterzensCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("taterzens")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .then(literal("config")
                        .then(literal("reload")
                                .executes(TaterzensCommand::reloadConfig)
                        )
                )
                .then(literal("wiki").executes(TaterzensCommand::wikiInfo))
        );
    }

    private static int wikiInfo(CommandContext<ServerCommandSource> context) {
        if(LUCKPERMS_ENABLED && !permissions$checkPermission(context.getSource(), MODID + ".wiki_info")) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }
        context.getSource().sendFeedback(
                successText("Visit %s for documentation.", new LiteralText("https://samolego.github.io/Taterzens/"))
                    .formatted(Formatting.GREEN)
                    .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://samolego.github.io/Taterzens/"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to see documentation.")))
                ),
                false
        );
        return 0;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        if(LUCKPERMS_ENABLED && !permissions$checkPermission(context.getSource(), MODID + ".config.reload")) {
            context.getSource().sendError(new TranslatableText("commands.help.failed").formatted(Formatting.RED));
            return -1;
        }

        config = TaterConfig.loadConfigFile(new File(taterDir + "/config.json"));
        lang = TaterLang.loadLanguageFile(new File(taterDir + "/" + config.language + ".json"));

        context.getSource().sendFeedback(
                new LiteralText(lang.success.configReloaded).formatted(Formatting.GREEN),
                false
        );
        return 0;
    }
}
