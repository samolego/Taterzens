package org.samo_lego.taterzens.commands.edit;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.samo_lego.taterzens.commands.NpcCommand;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.MessageArgument.message;
import static org.samo_lego.taterzens.Taterzens.FABRICTAILOR_LOADED;
import static org.samo_lego.taterzens.Taterzens.config;
import static org.samo_lego.taterzens.compatibility.LoaderSpecific.permissions$checkPermission;
import static org.samo_lego.taterzens.mixin.accessors.PlayerAccessor.getPLAYER_MODE_CUSTOMISATION;
import static org.samo_lego.taterzens.util.TextUtil.*;

public class SkinCommand {

    private static final String MINESKIN_API_URL = "https://api.mineskin.org/get/id/";
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

    public static void registerNode(LiteralCommandNode<CommandSourceStack> editNode) {
        LiteralCommandNode<CommandSourceStack> skinNode = literal("skin")
                .requires(src -> permissions$checkPermission(src, "taterzens.npc.edit.skin", config.perms.npcCommandPermissionLevel))
                .then(argument("mineskin URL | playername", message())
                        .executes(SkinCommand::setCustomSkin)
                )
                .executes(SkinCommand::copySkinLayers)
                .build();

        editNode.addChild(skinNode);
    }

    private static int setCustomSkin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String id = MessageArgument.getMessage(context, "mineskin URL | playername").getString();

        return NpcCommand.selectedTaterzenExecutor(source.getEntityOrException(), taterzen -> {
            // Shameless self-promotion
            if(config.fabricTailorAdvert) {
                if(FABRICTAILOR_LOADED) {
                    source.sendSuccess(translate("advert.fabrictailor.skin_command")
                                    .withStyle(ChatFormatting.GOLD)
                                    .withStyle(style ->
                                            style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skin set"))
                                    ),
                            false
                    );
                } else {
                    source.sendSuccess(translate("advert.fabrictailor")
                                    .withStyle(ChatFormatting.ITALIC)
                                    .withStyle(ChatFormatting.GOLD)
                                    .withStyle(style -> style
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/FabricTailor"))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translate("advert.tooltip.install", "FabricTailor")))
                                    ),
                            false
                    );
                }

            }

            if(id.contains(":") ) {
                THREADPOOL.submit(() -> {
                    String param = id.replaceAll("[^0-9]", "");
                    String mineskinUrl = MINESKIN_API_URL + param;
                    try {
                        URL url = new URL(mineskinUrl);
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setUseCaches(false);
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setRequestMethod("GET");

                        try (
                                InputStream is = connection.getInputStream();
                                InputStreamReader isr = new InputStreamReader(is);
                                BufferedReader br = new BufferedReader(isr)
                        ) {
                            String response = br.readLine();
                            String value = response.split("\"value\":\"")[1].split("\"")[0];
                            String signature = response.split("\"signature\":\"")[1].split("\"")[0];

                            CompoundTag skinTag = new CompoundTag();
                            skinTag.putString("value", value);
                            skinTag.putString("signature", signature);

                            taterzen.setSkinFromTag(skinTag);
                            taterzen.sendProfileUpdates();

                            source.sendSuccess(
                                    successText("taterzens.command.skin.fetched", id),
                                    false
                            );
                        }
                    } catch(MalformedURLException e) {
                        source.sendFailure(errorText("taterzens.error.invalid.url", mineskinUrl));
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                // Player's name
                GameProfile skinProfile = new GameProfile(null, id);
                SkullBlockEntity.updateGameprofile(skinProfile, taterzen::applySkin);
                context.getSource().sendSuccess(
                        successText("taterzens.command.skin.fetched", id),
                        false
                );
            }
        });
    }


    private static int copySkinLayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        return NpcCommand.selectedTaterzenExecutor(player, taterzen -> {
            Byte skinLayers = player.getEntityData().get(getPLAYER_MODE_CUSTOMISATION());
            taterzen.setSkinLayers(skinLayers);

            taterzen.sendProfileUpdates();
            source.sendSuccess(
                    successText("taterzens.command.skin.mirrored", taterzen.getName().getString()),
                    false
            );
        });
    }
}
