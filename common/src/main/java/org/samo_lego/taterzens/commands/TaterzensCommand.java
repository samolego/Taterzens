package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.samo_lego.taterzens.storage.TaterConfig;

import java.io.File;

import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class TaterzensCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("taterzens")
                .then(literal("config")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.taterzens_config_reload, config.perms.taterzensCommandPermissionLevel))
                        .then(literal("reload")
                                .executes(TaterzensCommand::reloadConfig)
                        )
                )
                .then(literal("wiki")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.taterzens_wiki, config.perms.taterzensCommandPermissionLevel))
                        .executes(TaterzensCommand::wikiInfo)
                )
        );
    }

    private static int wikiInfo(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                successText("Visit %s for documentation.", "https://samolego.github.io/Taterzens/")
                    .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://samolego.github.io/Taterzens/"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to see documentation.")))
                ),
                false
        );
        return 0;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        config = TaterConfig.loadConfigFile(new File(taterDir + "/config.json"));
        //lang = TaterConfig.loadLanguageFile();

        context.getSource().sendFeedback(
                translate("taterzens.command.config.success").formatted(Formatting.GREEN),
                false
        );
        return 0;
    }
}
