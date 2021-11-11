package org.samo_lego.taterzens.gui;

import com.mojang.datafixers.util.Pair;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
    private final int maxPages;
    private final boolean needsPages;
    private int currentPage = 0;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player the player to server this gui to
     * @param npc player's taterzen.
     */
    public MessageReorderGUI(ServerPlayer player, TaterzenNPC npc) {
        super(MenuType.GENERIC_9x6, player, false);

        this.messages = npc.getMessages();
        this.maxPages = this.messages.size() / this.getSize();
        this.setTitle(new TranslatableComponent("chat_screen.title").append(": ").withStyle(ChatFormatting.YELLOW).append(npc.getName().copy()));

        this.needsPages = this.messages.size() > this.getSize();

        int i = needsPages ? 9 : 0;

        do {
            // - 9 as first row is occupied but we want to have index 0 at first element
            this.setSlotRedirect(i, new MessageSlot(this, needsPages ?  i - 9 : i));
        } while (++i < this.getSize());

        if (this.needsPages) {
            // Oh my, this taterzen has a lot to tell
            // we need more pages!

            // Info (which page)
            ItemStack info = new ItemStack(Items.PAPER);
            info.setTag(customData.copy());
            info.setHoverName(getCurrentPageMarker());
            info.enchant(null, 0);

            this.setSlot(3, info);

            // Previous page
            ItemStack back = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
            back.setTag(customData.copy());
            back.setHoverName(new TranslatableComponent("spectatorMenu.previous_page"));
            back.enchant(null, 0);

            GuiElement previousScreenButton = new GuiElement(back, (index, type1, action) -> {
                if (--this.currentPage < 0)
                    this.currentPage = 0;
                info.setHoverName(getCurrentPageMarker());
            });
            this.setSlot(0, previousScreenButton);

            // Next page
            ItemStack next = new ItemStack(Items.LIGHT_BLUE_GLAZED_TERRACOTTA);
            next.setTag(customData.copy());
            next.setHoverName(new TranslatableComponent("spectatorMenu.next_page"));
            next.enchant(null, 0);

            GuiElement nextScreenButton = new GuiElement(next, (_i, _clickType, _slotActionType) -> {
                if (++this.currentPage > this.maxPages)
                    this.currentPage = this.maxPages;
                info.setHoverName(getCurrentPageMarker());
            });
            this.setSlot(1, nextScreenButton);


            // Close screen button
            ItemStack close = new ItemStack(Items.STRUCTURE_VOID);
            close.setTag(customData.copy());
            close.setHoverName(new TranslatableComponent("spectatorMenu.close"));
            close.enchant(null, 0);

            GuiElement closeScreenButton = new GuiElement(close, (_i, _clickType, _slotActionType) -> player.closeContainer());
            this.setSlot(8, closeScreenButton);
        }
    }

    /**
     * Gest current page info (Oage X of Y)
     * @return translated page info text.
     */
    private TranslatableComponent getCurrentPageMarker() {
        return new TranslatableComponent("book.pageIndicator", this.currentPage + 1, this.maxPages + 1);
    }


    @Override
    public int getContainerSize() {
        return 9 * 6;
    }

    @Override
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    private int getSlot2MessageIndex(int slotIndex) {
        return this.needsPages ? this.currentPage * this.getSize() + slotIndex : slotIndex;
    }

    @Override
    public ItemStack getItem(int index) {
        ItemStack itemStack;
        index = getSlot2MessageIndex(index);
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
        index = getSlot2MessageIndex(index);
        if(index < this.messages.size()) {
            Pair<Component, Integer> removed = this.messages.remove(index);
            itemStack.setHoverName(removed.getFirst());
        }

        return itemStack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (!stack.isEmpty()) {
            index = getSlot2MessageIndex(index);
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
