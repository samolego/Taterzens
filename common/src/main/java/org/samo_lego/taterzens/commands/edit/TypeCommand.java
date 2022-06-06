package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.compatibility.DisguiseLibCompatibility;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.synchronization.SuggestionProviders.SUMMONABLE_ENTITIES;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.ModDiscovery.DISGUISELIB_LOADED;
import static org.samo_lego.taterzens.util.TextUtil.successText;
import static org.samo_lego.taterzens.util.TextUtil.translate;

public class TypeCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> typeNode = literal("type")
                .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.entity_type", config.perms.npcCommandPermissionLevel))
                .then(argument("entity type", EntitySummonArgument.id())
                        .suggests(SUMMONABLE_ENTITIES)
                        .executes(TypeCommand::changeType)
                        .then(argument("nbt", CompoundTagArgument.compoundTag())
                                .executes(TypeCommand::changeType)
                        )
                )
                .then(literal("minecraft:player")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.entity_type", config.perms.npcCommandPermissionLevel))
                        .executes(TypeCommand::resetType)
                )
                .then(literal("player")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.entity_type", config.perms.npcCommandPermissionLevel))
                        .executes(TypeCommand::resetType)
                )
                .then(literal("reset")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.edit.entity_type", config.perms.npcCommandPermissionLevel))
                        .executes(TypeCommand::resetType)
                )
                .build();

        editNode.addChild(typeNode);
    }

    private static int changeType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if(!DISGUISELIB_LOADED) {
            source.sendFailure(translate("advert.disguiselib.required")
                    .withStyle(ChatFormatting.RED)
                    .withStyle(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("advert.tooltip.install", "DisguiseLib")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/disguiselib"))
                    )
            );
            return -1;
        }

        ResourceLocation disguise = EntitySummonArgument.getSummonableEntity(context, "entity type");
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            CompoundTag nbt;
            try {
                nbt = CompoundTagArgument.getCompoundTag(context, "nbt").copy();
            } catch(IllegalArgumentException ignored) {
                nbt = new CompoundTag();
            }
            nbt.putString("id", disguise.toString());

            EntityType.loadEntityRecursive(nbt, source.getLevel(), (entityx) -> {
                DisguiseLibCompatibility.disguiseAs(taterzen, entityx);
                source.sendSuccess(
                        translate(
                                "taterzens.command.entity_type.set",
                                Component.translatable(entityx.getType().getDescriptionId()).withStyle(ChatFormatting.YELLOW)
                        ).withStyle(ChatFormatting.GREEN),
                        false
                );
                return entityx;
            });
        });
    }

    private static int resetType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if(!DISGUISELIB_LOADED) {
            source.sendFailure(translate("advert.disguiselib.required")
                    .withStyle(ChatFormatting.RED)
                    .withStyle(style -> style
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("advert.tooltip.install", "DisguiseLib")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/disguiselib"))
                    )
            );
            return -1;
        }
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            DisguiseLibCompatibility.clearDisguise(taterzen);
            source.sendSuccess(
                    successText("taterzens.command.entity_type.reset", taterzen.getName().getString()),
                    false
            );
        });
    }

}
