package org.samo_lego.taterzens.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.taterzens.mixin.accessors.MappedRegistryAccessor;
import org.samo_lego.taterzens.npc.TaterzenNPC;

import static org.samo_lego.taterzens.Taterzens.config;

public class MessagesEditGUI extends ListItemsGUI<Pair<Component, Integer>> {
    private static final int REGISTRY_ITEMS_SIZE = ((MappedRegistryAccessor) Registry.ITEM).getById().size();

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player              the player to server this gui to.
     * @param taterzen             player's taterzen.
     */
    public MessagesEditGUI(ServerPlayer player, TaterzenNPC taterzen) {
        super(player, taterzen.getName(), "chat_screen.title", taterzen.getMessages());

        int i = 9;
        do {
            // - 9 as first row is occupied but we want to have index 0 at first element
            this.setSlotRedirect(i, new RedirectedSlot(this, i - 9));
        } while (++i < this.getSize());
    }

    private ItemStack getItem(Pair<Component, Integer> pair) {
        Component message = pair.getFirst();
        // Gets an item from registry depending on message string hash
        int i = Math.abs(message.getString().hashCode());
        Item item = Item.byId(i % REGISTRY_ITEMS_SIZE);
        if (item.equals(Items.AIR))
            item = Items.STONE;

        ItemStack itemStack = new ItemStack(item);
        itemStack.setTag(customData.copy());
        itemStack.setHoverName(message);

        return itemStack;
    }


    @Override
    public ItemStack getItem(int index) {
        ItemStack itemStack;
        index = getSlot2MessageIndex(index);

        if(index < this.items.size()) {
            itemStack = getItem(this.items.get(index));
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
        index = getSlot2MessageIndex(index);
        if(index < this.items.size()) {
            Pair<Component, Integer> removed = this.items.remove(index);
            itemStack.setHoverName(this.getItem(removed).getHoverName());
        }

        return itemStack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (!stack.isEmpty()) {
            index = getSlot2MessageIndex(index);
            if (index > items.size())
                index = items.size();
            this.items.add(index, new Pair<>(stack.getHoverName(), config.messages.messageDelay));
            stack.setCount(0);
        }
    }

}
