package org.samo_lego.taterzens.gui;

import com.mojang.datafixers.util.Pair;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.taterzens.mixin.accessors.MappedRegistryAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import java.util.List;

import static org.samo_lego.taterzens.Taterzens.config;

public class MessageReorderGUI extends SimpleGui implements Container {
    private static final CompoundTag customData = new CompoundTag();
    private static final int registryItemsSize = ((MappedRegistryAccessor) Registry.ITEM).getById().size();
    private final List<Pair<Component, Integer>> messages;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player the player to server this gui to
     * @param npc player's taterzen.
     */
    public MessageReorderGUI(ServerPlayer player, TaterzenNPC npc) {
        super(MenuType.GENERIC_9x6, player, false);

        this.messages = npc.getMessages();

        for (int i = 0; i  < this.getSize(); ++i) {
            this.setSlotRedirect(i, new MessageSlot(this, i));
        }
    }



    @Override
    public int getContainerSize() {
        return 9 * 6;
    }

    @Override
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        ItemStack itemStack;
        if(index < this.messages.size()) {
            Component message = this.messages.get(index).getFirst();

            // Gets an item from registry depending on message string hash
            int i = Math.abs(message.getString().hashCode());
            Item item = Item.byId(i % registryItemsSize);
            if (item.equals(Items.AIR))
                item = Items.STONE;

            itemStack = new ItemStack(item);
            itemStack.setTag(customData.copy());
            itemStack.setHoverName(message);
        } else {
            itemStack = ItemStack.EMPTY;
        }
        return itemStack;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return this.removeItemNoUpdate(index);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack itemStack = this.getItem(index);
        if(index < this.messages.size()) {
            Pair<Component, Integer> removed = this.messages.remove(index);
            itemStack.setHoverName(removed.getFirst());
        }

        return itemStack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (!stack.isEmpty()) {
            if (index > messages.size())
                index = messages.size();
            this.messages.add(index, new Pair<>(stack.getHoverName(), config.messages.messageDelay));
            stack.setCount(0);
        }
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        this.messages.clear();
    }

    static {
        customData.putInt("CustomModelData", config.guiItemModelData);
        customData.putInt("HideFlags", 127);
    }
}
