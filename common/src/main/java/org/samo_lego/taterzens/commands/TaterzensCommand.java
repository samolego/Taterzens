package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.samo_lego.taterzens.storage.ConfigFieldList;
import org.samo_lego.taterzens.storage.TaterConfig;
import org.samo_lego.taterzens.util.LanguageUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

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
import static org.apache.logging.log4j.core.impl.ThreadContextDataInjector.copyProperties;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.storage.ConfigFieldList.populateFields;
import static org.samo_lego.taterzens.util.LanguageUtil.LANG_LIST;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class TaterzensCommand {
    private static final SuggestionProvider<CommandSourceStack> AVAILABLE_LANGUAGES;
    private static final ConfigFieldList FIELDS;

    private static final File CONFIG_FILE = new File(taterDir + "/config.json");
    private static final String DOCS_URL = "https://samolego.github.io/Taterzens/";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        // root node
        LiteralCommandNode<CommandSourceStack> taterzensNode = dispatcher.register(literal("taterzens")
                .then(literal("wiki")
                        .requires(src -> permissions$checkPermission(src, "taterzens.wiki_info", config.perms.taterzensCommandPermissionLevel))
                        .executes(TaterzensCommand::wikiInfo)
                )
        );

        // config node
        LiteralCommandNode<CommandSourceStack> configNode = literal("config")
                .requires(src -> permissions$checkPermission(src, "taterzens.config.edit", config.perms.taterzensCommandPermissionLevel))
                .then(literal("reload")
                        .requires(src -> permissions$checkPermission(src, "taterzens.config.reload", config.perms.taterzensCommandPermissionLevel))
                        .executes(TaterzensCommand::reloadConfig)
                )
                .build();

        LiteralCommandNode<CommandSourceStack> editNode = literal("edit")
                .then(literal("language")
                        .requires(src -> permissions$checkPermission(src, "taterzens.config.edit.lang", config.perms.taterzensCommandPermissionLevel))
                        .then(argument("language", word())
                                .suggests(AVAILABLE_LANGUAGES)
                                .executes(TaterzensCommand::setLang)
                        )
                )
                .build();

        // Dynamically generate command
        generateNestedCommand(editNode, FIELDS);

        // Register nodes
        configNode.addChild(editNode);
        taterzensNode.addChild(configNode);
    }

    public static int editConfigAttribute(CommandContext<CommandSourceStack> context, Object parent, Field attribute, Object value, Predicate<Field> fieldConsumer) {
        attribute.setAccessible(true);
        boolean result = fieldConsumer.test(attribute);

        String option = StringUtils.difference(TaterConfig.class.getName(), parent.getClass().getName());

        if(!option.isEmpty()) {
            option = option.substring(1);
            option = option.replaceAll("\\$", ".") + ".";
        }
        option += attribute.getName();

        if(result) {
            config.saveConfigFile(CONFIG_FILE);

            context.getSource().sendSuccess(successText("taterzens.command.config.edit.success", option, value.toString()), false);
        } else {
            context.getSource().sendFailure(errorText("taterzens.command.config.edit.failure", option));
        }

        return result ? 1 : 0;
    }

    private static int editConfigBoolean(CommandContext<CommandSourceStack> context, Object parent, Field attribute) {
        boolean value = BoolArgumentType.getBool(context, "value");

        return editConfigAttribute(context, parent, attribute, value, field -> {
            try {
                field.setBoolean(parent, value);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    private static int editConfigInt(CommandContext<CommandSourceStack> context, Object parent, Field attribute) {
        int value = IntegerArgumentType.getInteger(context, "value");

        return editConfigAttribute(context, parent, attribute, value, field -> {
            try {
                field.setInt(parent, value);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    private static int editConfigFloat(CommandContext<CommandSourceStack> context, Object parent, Field attribute) {
        float value = FloatArgumentType.getFloat(context, "value");

        return editConfigAttribute(context, parent, attribute, value, field -> {
            try {
                field.setFloat(parent, value);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    private static int editConfigObject(CommandContext<CommandSourceStack> context, Object parent, Field attribute) {
        String value = StringArgumentType.getString(context, "value");

        return editConfigAttribute(context, parent, attribute, value, field -> {
            try {
                field.set(parent, value);
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        });
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
            String url = "https://github.com/samolego/Taterzens#translation-contributions";
            source.sendFailure(
                    errorText("taterzens.command.language.404", language, url)
                    .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                    )
            );
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
        config.reload(CONFIG_FILE);
        LanguageUtil.setupLanguage();
    }

    private static void generateNestedCommand(LiteralCommandNode<CommandSourceStack> root, ConfigFieldList fields) {
        fields.booleans.forEach(attribute -> {
            LiteralCommandNode<CommandSourceStack> node = literal(attribute.getName())
                    .then(argument("value", BoolArgumentType.bool())
                            .executes(context -> editConfigBoolean(context, fields.parent, attribute))
                    )
                    .build();
            root.addChild(node);
        });

        fields.integers.forEach(attribute -> {
            LiteralCommandNode<CommandSourceStack> node = literal(attribute.getName())
                    .then(argument("value", IntegerArgumentType.integer())
                            .executes(context -> editConfigInt(context, fields.parent, attribute))
                    )
                    .build();
            root.addChild(node);
        });

        fields.floats.forEach(attribute -> {
            LiteralCommandNode<CommandSourceStack> node = literal(attribute.getName())
                    .then(argument("value", FloatArgumentType.floatArg())
                            .executes(context -> editConfigFloat(context, fields.parent, attribute))
                    )
                    .build();
            root.addChild(node);
        });

        fields.strings.forEach(attribute -> {
            LiteralCommandNode<CommandSourceStack> node = literal(attribute.getName())
                    .then(argument("value", StringArgumentType.greedyString())
                            .executes(context -> editConfigObject(context, fields.parent, attribute))
                    )
                    .build();
            root.addChild(node);
        });

        fields.nestedFields.forEach(generator -> {
            LiteralCommandNode<CommandSourceStack> node = literal(generator.nodeName).build();
            generateNestedCommand(node, generator);
            root.addChild(node);
        });
    }


    static {
        FIELDS = populateFields(config, "edit");

        AVAILABLE_LANGUAGES = SuggestionProviders.register(
                new ResourceLocation(MODID, "languages"),
                (context, builder) ->
                        SharedSuggestionProvider.suggest(LANG_LIST, builder)
        );
    }
}
