package org.samo_lego.taterzens.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RedirectedSlot extends Slot {
    public RedirectedSlot(Container container, int index) {
        super(container, index, 0, 0);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty()) {
            this.set(carried);
            carried.setCount(0);
            return false;
        }
        return true;
    }
}
