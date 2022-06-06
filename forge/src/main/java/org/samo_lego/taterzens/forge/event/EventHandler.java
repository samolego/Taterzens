package org.samo_lego.taterzens.forge.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.samo_lego.taterzens.Taterzens;
import org.samo_lego.taterzens.event.BlockEvent;

import static org.samo_lego.taterzens.Taterzens.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
    public EventHandler() {
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        // check in if clause prevents crash (check for server side) on clients
        // and prevents executing event handler twice (check for main hand)
        if (event.getSide().isServer() && event.getHand() == InteractionHand.MAIN_HAND) {
            Player player = event.getPlayer();
            if (BlockEvent.onBlockInteract(player, player.getCommandSenderWorld(), event.getPos()) == InteractionResult.FAIL) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        Taterzens.registerCommands(dispatcher);
    }
}
