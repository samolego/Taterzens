package org.samo_lego.taterzens.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import org.samo_lego.taterzens.common.Taterzens;
import org.samo_lego.taterzens.common.util.LanguageUtil;
import xyz.nucleoid.server.translations.api.language.ServerLanguage;
import xyz.nucleoid.server.translations.impl.ServerTranslations;

import java.net.URI;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.common.Taterzens.MOD_ID;
import static org.samo_lego.taterzens.common.Taterzens.config;
import static org.samo_lego.taterzens.common.compatibility.ModDiscovery.SERVER_TRANSLATIONS_LOADED;
import static org.samo_lego.taterzens.common.util.LanguageUtil.LANG_LIST;
import static org.samo_lego.taterzens.common.util.TextUtil.*;

public class TaterzensCommand {
    private static final SuggestionProvider<CommandSourceStack> AVAILABLE_LANGUAGES;

    private static final String DOCS_URL = "https://samolego.github.io/Taterzens/";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // root node
        LiteralCommandNode<CommandSourceStack> taterzensNode = dispatcher.register(literal("taterzens")
                .then(literal("wiki")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.wiki_info", config.perms.taterzensCommandPermissionLevel))
                        .executes(TaterzensCommand::wikiInfo)
                )
        );

        // config node
        LiteralCommandNode<CommandSourceStack> configNode = literal("config")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.config", config.perms.taterzensCommandPermissionLevel))
                .then(literal("reload")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.config.reload", config.perms.taterzensCommandPermissionLevel))
                        .executes(TaterzensCommand::reloadConfig)
                )
                .build();

        LiteralCommandNode<CommandSourceStack> editNode = literal("edit")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.config.edit", config.perms.taterzensCommandPermissionLevel))
                .then(literal("language")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.config.edit.lang", config.perms.taterzensCommandPermissionLevel))
                        .then(argument("language", word())
                                .suggests(AVAILABLE_LANGUAGES)
                                .executes(TaterzensCommand::setLang)
                        )
                )
                .build();


        // Register config edit commands
        config.generateCommand(editNode);

        // Register nodes
        configNode.addChild(editNode);
        taterzensNode.addChild(configNode);
    }

    private static int setLang(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String language = StringArgumentType.getString(context, "language");

        // Set language for config editing system
        ServerTranslations.INSTANCE.setSystemLanguage(ServerLanguage.getLanguage(language).definition());

        if(LANG_LIST.contains(language)) {
            config.language = language;
            config.save();

            LanguageUtil.setupLanguage();
            source.sendSuccess(() -> successText("taterzens.command.language.success", language), false);
            if (SERVER_TRANSLATIONS_LOADED) {
                source.sendSuccess(() -> successText("taterzens.command.language.server_translations_hint.1"), false);
                source.sendSuccess(() -> successText("taterzens.command.language.server_translations_hint.2"), false);
            }

        } else {
            String url = "https://github.com/samolego/Taterzens#translation-contributions";
            source.sendFailure(
                    errorText("taterzens.command.language.404", language, url)
                    .withStyle(style -> style
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
                    )
            );
        }

        return 1;
    }

    private static int wikiInfo(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
                        successText("taterzens.command.wiki", DOCS_URL)
                                .withStyle(ChatFormatting.GREEN)
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(DOCS_URL)))
                                        .withHoverEvent(new HoverEvent.ShowText(translate("taterzens.tooltip.see_docs")))
                                ),
                false
        );
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        reloadConfig();

        context.getSource().sendSuccess(() ->
                        translate("taterzens.command.config.success").withStyle(ChatFormatting.GREEN),
                false
        );
        return 1;
    }

    /**
     * Reloads the config and language objects.
     */
    private static void reloadConfig() {
        config.reload();
        LanguageUtil.setupLanguage();
    }


    static {
        AVAILABLE_LANGUAGES = SuggestionProviders.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "languages"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(LANG_LIST, builder)
        );
    }
}
