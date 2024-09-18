package org.samo_lego.taterzens.fabric.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

import java.util.List;

import static org.samo_lego.taterzens.common.Taterzens.config;

public class MessagesEditGUI extends ListItemsGUI {
    private final List<Pair<Component, Integer>> messages;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player              the player to server this gui to.
     * @param taterzen             player's taterzen.
     */
    public MessagesEditGUI(ServerPlayer player, TaterzenNPC taterzen) {
        super(player, taterzen.getName(), "chat_screen.title");

        this.messages = taterzen.getMessages();

        int i = 9;
        do {
            // - 9 as first row is occupied but we want to have index 0 at first element
            this.setSlotRedirect(i, new RedirectedSlot(this, i - 9));
        } while (++i < this.getSize());
    }

    private ItemStack getItem(Pair<Component, Integer> pair) {
        Component message = pair.getFirst();
        ItemStack itemStack = new ItemStack(getFromName(message.getString()));

        itemStack.applyComponents(customData);
        itemStack.set(DataComponents.CUSTOM_NAME, message);

        return itemStack;
    }


    @Override
    public ItemStack getItem(int index) {
        ItemStack itemStack;
        index = this.getActualPageIndex(index);

        if(index < this.messages.size()) {
            itemStack = getItem(this.messages.get(index));
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
        index = this.getActualPageIndex(index);
        if(index < this.messages.size()) {
            Pair<Component, Integer> removed = this.messages.remove(index);
            itemStack.set(DataComponents.CUSTOM_NAME, this.getItem(removed).getHoverName());
        }

        return itemStack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (!stack.isEmpty()) {
            index = this.getActualPageIndex(index);
            if (index > messages.size())
                index = messages.size();
            this.messages.add(index, new Pair<>(stack.getHoverName(), config.messages.messageDelay));
            stack.setCount(0);
        }
    }

    @Override
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    @Override
    public void clearContent() {
        this.messages.clear();
    }

    @Override
    public int getMaxPages() {
        if (this.messages == null)
            return 0;
        return this.messages.size() / this.getSize();
    }
}
