package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.util.LanguageUtil;

import java.io.File;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.LanguageUtil.LANG_LIST;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class TaterzensCommand {
    private static final SuggestionProvider<CommandSourceStack> AVAILABLE_LANGUAGES;
    private static final File CONFIG_FILE = new File(taterDir + "/config.json");
    private static final String DOCS_URL = "https://samolego.github.io/Taterzens/";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
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

    private static int setLang(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String language = StringArgumentType.getString(context, "language");

        if(LANG_LIST.contains(language)) {
            config.language = language;
            config.saveConfigFile(CONFIG_FILE);

            LanguageUtil.setupLanguage();
            source.sendSuccess(successText("taterzens.command.language.success", language), false);
            if(SERVER_TRANSLATIONS_LOADED) {
                source.sendSuccess(successText("taterzens.command.language.server_translations_hint.1"), false);
                source.sendSuccess(successText("taterzens.command.language.server_translations_hint.2"), false);
            }

        } else {
            source.sendFailure(errorText("taterzens.command.language.404", language, "https://github.com/samolego/Taterzens#translation-contributions"));
        }

        return 0;
    }

    private static int wikiInfo(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
                successText("taterzens.command.wiki", DOCS_URL)
                        .withStyle(ChatFormatting.GREEN)
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DOCS_URL))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("taterzens.tooltip.see_docs")))
                        ),
                false
        );
        return 0;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        reloadConfig();

        context.getSource().sendSuccess(
                translate("taterzens.command.config.success").withStyle(ChatFormatting.GREEN),
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
                new ResourceLocation(MODID, "languages"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(LANG_LIST, builder)
        );
    }
}
