package org.samo_lego.taterzens.commands.edit;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import org.samo_lego.taterzens.commands.NpcCommand;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.PERMISSIONS;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class TagsCommand {
    public static void registerNode(LiteralCommandNode<ServerCommandSource> editNode) {
        LiteralCommandNode<ServerCommandSource> tagsNode = literal("tags")
                .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_tags, config.perms.npcCommandPermissionLevel))
                .then(literal("leashable")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_tags_leashable, config.perms.npcCommandPermissionLevel))
                        .then(argument("leashable", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean leashable = BoolArgumentType.getBool(ctx, "leashable");
                                    return setTag(ctx, "leashable", leashable, npc -> npc.setLeashable(leashable));
                                })
                        )
                )
                .then(literal("pushable")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_tags_pushable, config.perms.npcCommandPermissionLevel))
                        .then(argument("pushable", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean pushable = BoolArgumentType.getBool(ctx, "pushable");
                                    return setTag(ctx, "pushable", pushable, npc -> npc.setPushable(pushable));
                                })
                        )
                )
                .then(literal("jumpWhileAttacking")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_tags_jumpWhileAttacking, config.perms.npcCommandPermissionLevel))
                        .then(argument("perform jumps", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean jumpWhileAttacking = BoolArgumentType.getBool(ctx, "perform jumps");
                                    return setTag(ctx, "jumpWhileAttacking", jumpWhileAttacking, npc -> npc.setPerformAttackJumps(jumpWhileAttacking));
                                })
                        )
                )
                .then(literal("allowEquipmentDrops")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_equipment_equipmentDrops, config.perms.npcCommandPermissionLevel))
                        .then(argument("drop", BoolArgumentType.bool()).executes(EquipmentCommand::setEquipmentDrops))
                )
                .then(literal("sneakNameType")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_tags_sneakName, config.perms.npcCommandPermissionLevel))
                        .then(argument("sneak type name", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean sneakNameType = BoolArgumentType.getBool(ctx, "sneak type name");
                                    return setTag(ctx, "sneakNameType", sneakNameType, npc -> npc.setSneaking(sneakNameType));
                                })
                        )
                )
                .then(literal("allowSounds")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_edit_tags_allowSounds, config.perms.npcCommandPermissionLevel))
                        .then(argument("allow sounds", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean allowSounds = BoolArgumentType.getBool(ctx, "allow sounds");
                                    return setTag(ctx, "allowSounds", allowSounds, npc -> npc.setAllowSounds(allowSounds));
                                })
                        )
                )
                .build();

        editNode.addChild(tagsNode);
    }

    private static int setTag(CommandContext<ServerCommandSource> context, String flagName, boolean flagValue, Consumer<TaterzenNPC> flag) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            flag.accept(taterzen);
            source.sendFeedback(successText("taterzens.command.flag.changed", flagName, String.valueOf(flagValue)), false);
        });
    }
}
