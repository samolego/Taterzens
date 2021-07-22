package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.util.LanguageUtil;

import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.LanguageUtil.LANG_LIST;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class TaterzensCommand {
    private static final SuggestionProvider<ServerCommandSource> AVAILABLE_LANGUAGES;
    private static final File CONFIG_FILE = new File(taterDir + "/config.json");
    private static final String DOCS_URL = "https://samolego.github.io/Taterzens/";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("taterzens")
                .then(literal("config")
                        .requires(src -> permissions$checkPermission(src,  "taterzens.config.edit", config.perms.taterzensCommandPermissionLevel))
                        .then(literal("reload")
                                .requires(src -> permissions$checkPermission(src,  "taterzens.config.reload", config.perms.taterzensCommandPermissionLevel))
                                .executes(TaterzensCommand::reloadConfig)
                        )
                        .then(literal("edit")
                                .then(literal("language")
                                        .requires(src -> permissions$checkPermission(src,  "taterzens.config.edit.lang", config.perms.taterzensCommandPermissionLevel))
                                        .then(argument("language", word())
                                                .suggests(AVAILABLE_LANGUAGES)
                                                .executes(TaterzensCommand::setLang)
                                        )
                                )
                        )
                )
                .then(literal("wiki")
                        .requires(src -> permissions$checkPermission(src, "taterzens.wiki_info", config.perms.taterzensCommandPermissionLevel))
                        .executes(TaterzensCommand::wikiInfo)
                )
        );
    }

    private static int setLang(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String language = StringArgumentType.getString(context, "language");

        if(LANG_LIST.contains(language)) {
            config.language = language;
            config.saveConfigFile(CONFIG_FILE);

            LanguageUtil.setupLanguage();
            source.sendFeedback(successText("taterzens.command.language.success", language), false);
            if(SERVER_TRANSLATIONS_LOADED) {
                source.sendFeedback(successText("taterzens.command.language.server_translations_hint.1"), false);
                source.sendFeedback(successText("taterzens.command.language.server_translations_hint.2"), false);
            }

        } else {
            source.sendError(errorText("taterzens.command.language.404", language));
        }

        return 0;
    }

    private static int wikiInfo(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                successText("taterzens.command.wiki", DOCS_URL)
                        .formatted(Formatting.GREEN)
                        .styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DOCS_URL))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.see_docs")))
                        ),
                false
        );
        return 0;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        reloadConfig();

        context.getSource().sendFeedback(
                translate("taterzens.command.config.success").formatted(Formatting.GREEN),
                false
        );
        return 0;
    }

    private static void reloadConfig() {
        config = TaterConfig.loadConfigFile(new File(taterDir + "/config.json"));
        LanguageUtil.setupLanguage();
    }


    static {
        AVAILABLE_LANGUAGES = SuggestionProviders.register(
                new Identifier(MODID, "languages"),
                (context, builder) ->
                        CommandSource.suggestMatching(LANG_LIST, builder)
        );
    }
}
