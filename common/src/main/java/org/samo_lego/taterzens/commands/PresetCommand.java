package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.interfaces.TaterzenEditor;

import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.samo_lego.taterzens.Taterzens.*;
import static org.samo_lego.taterzens.api.TaterzensAPI.getPresets;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class PresetCommand {
    public static void registerNode(LiteralCommandNode<ServerCommandSource> npcNode) {
        LiteralCommandNode<ServerCommandSource> presetNode = literal("preset")
                .then(literal("save")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_preset_save, config.perms.npcCommandPermissionLevel))
                        .then(argument("preset name", word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(getPresets(), builder))
                                .executes(PresetCommand::saveTaterzenToPreset)
                        )
                )
                .then(literal("load")
                        .requires(src -> permissions$checkPermission(src, PERMISSIONS.npc_preset_load, config.perms.npcCommandPermissionLevel))
                        .then(argument("preset name", word())
                                .suggests((context, builder) -> CommandSource.suggestMatching(getPresets(), builder))
                                .executes(PresetCommand::loadTaterzenFromPreset)
                        )
                )
                .build();

        npcNode.addChild(presetNode);
    }

    private static int loadTaterzenFromPreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String filename = StringArgumentType.getString(context, "preset name") + ".json";
        File preset = new File(presetsDir + "/" + filename);
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if(preset.exists()) {
            return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
                Vec3d pos = source.getPosition();
                Vec2f rotation = source.getRotation();
                taterzen.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), rotation.x, rotation.y);

                source.getWorld().spawnEntity(taterzen);

                ((TaterzenEditor) player).selectNpc(taterzen);

                source.sendFeedback(
                        successText("taterzens.command.preset.import.success", filename),
                        false
                );
            });
        } else {
            source.sendError(
                    errorText("taterzens.command.preset.import.error.404", filename)
            );
        }
        return -1;
    }

    private static int saveTaterzenToPreset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getPlayer(), taterzen -> {
            String filename = StringArgumentType.getString(context, "preset name") + ".json";
            File preset = new File(presetsDir + "/" + filename);
            TaterzensAPI.saveTaterzenToPreset(taterzen, preset);

            source.sendFeedback(
                    successText("taterzens.command.preset.export.success", filename),
                    false
            );
        });
    }
}
