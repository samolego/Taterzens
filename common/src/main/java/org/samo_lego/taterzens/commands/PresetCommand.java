package org.samo_lego.taterzens.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.api.TaterzensAPI;
import org.samo_lego.taterzens.interfaces.ITaterzenEditor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.api.TaterzensAPI.getPresets;
import static org.samo_lego.taterzens.util.TextUtil.errorText;
import static org.samo_lego.taterzens.util.TextUtil.successText;

public class PresetCommand {
    public static void registerNode(LiteralCommandNode<CommandSourceStack> npcNode) {
        LiteralCommandNode<CommandSourceStack> presetNode = literal("preset")
                .then(literal("save")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.preset.save", config.perms.npcCommandPermissionLevel))
                        .then(argument("preset name", word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getPresets(), builder))
                                .executes(PresetCommand::saveTaterzenToPreset)
                        )
                )
                .then(literal("load")
                        .requires(src -> Taterzens.getInstance().getPlatform().checkPermission(src, "taterzens.npc.preset.load", config.perms.npcCommandPermissionLevel))
                        .then(argument("preset name", word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getPresets(), builder))
                                .executes(PresetCommand::loadTaterzenFromPreset)
                        )
                )
                .build();

        npcNode.addChild(presetNode);
    }

    private static int loadTaterzenFromPreset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String filename = StringArgumentType.getString(context, "preset name") + ".json";
        File preset = new File(Taterzens.getInstance().getPresetDirectory() + "/" + filename);
        CommandSourceStack source = context.getSource();

        if(preset.exists()) {
            TaterzenNPC taterzenNPC = TaterzensAPI.loadTaterzenFromPreset(preset, source.getLevel());
            if(taterzenNPC != null) {
                Vec3 pos = source.getPosition();
                Vec2 rotation = source.getRotation();
                taterzenNPC.moveTo(pos.x(), pos.y(), pos.z(), rotation.x, rotation.y);
                source.getLevel().addFreshEntity(taterzenNPC);

                ((ITaterzenEditor) source.getPlayerOrException()).selectNpc(taterzenNPC);

                taterzenNPC.sendProfileUpdates();

                source.sendSuccess(
                        successText("taterzens.command.preset.import.success", filename),
                        false
                );
            }
        } else {
            source.sendFailure(
                    errorText("taterzens.command.preset.import.error.404", filename)
            );
        }

        return -1;
    }

    private static int saveTaterzenToPreset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            String filename = StringArgumentType.getString(context, "preset name") + ".json";
            File preset = new File(Taterzens.getInstance().getPresetDirectory() + "/" + filename);
            TaterzensAPI.saveTaterzenToPreset(taterzen, preset);

            source.sendSuccess(
                    successText("taterzens.command.preset.export.success", filename),
                    false
            );
        });
    }
}
