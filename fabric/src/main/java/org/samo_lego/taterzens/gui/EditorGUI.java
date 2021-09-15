package org.samo_lego.taterzens.gui;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EditorGUI extends SimpleGui {

    private static final ItemStack YES_BUTTON = new ItemStack(Items.GREEN_STAINED_GLASS_PANE);
    private static final ItemStack NO_BUTTON = new ItemStack(Items.RED_STAINED_GLASS_PANE);

    public EditorGUI(CommandContext<CommandSourceStack> context, ServerPlayer player, EditorGUI previousScreen, List<CommandNode<CommandSourceStack>> parentNodes) {
        super(MenuType.GENERIC_9x6, player, true);
        CommandNode<CommandSourceStack> parentNode = parentNodes.get(parentNodes.size() - 1);

        this.setTitle(new TextComponent("NPC editing " + parentNode.getName()));
        this.setAutoUpdate(true);


        ItemStack back = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
        back.setHoverName(new TextComponent("Back"));

        GuiElement backScreenButton = new GuiElement(back, (i, clickType, slotActionType) -> {
            if (previousScreen == null)
                player.closeContainer();
            else
                previousScreen.open();
        });
        this.setSlot(52, backScreenButton);

        ItemStack close = new ItemStack(Items.BARRIER);
        close.setHoverName(new TextComponent("Close"));


        GuiElement closeScreenButton = new GuiElement(close, (i, clickType, slotActionType) -> {
            player.closeContainer();
        });
        this.setSlot(53, closeScreenButton);

        AtomicInteger i = new AtomicInteger();

        // Looping through command node
        parentNode.getChildren().forEach(node -> {
            // Tracking current command "path"
            ArrayList<CommandNode<CommandSourceStack>> parents = new ArrayList<>(parentNodes);
            parents.add(node);

            ItemStack stack = new ItemStack(Items.GLOW_ITEM_FRAME);
            stack.setHoverName(new TextComponent(node.getName()));

            this.setSlot(i.getAndIncrement(), new GuiElement(stack, (index, clickType, slotActionType) -> {

                ItemStack item = this.getSlot(index).getItemStack();

                Command<CommandSourceStack> command = node.getCommand();
                if (clickType == ClickType.MOUSE_LEFT) {
                    Collection<CommandNode<CommandSourceStack>> children = node.getChildren();

                    if(!children.isEmpty()) {
                        EditorGUI childGUI = new EditorGUI(context, player, this, parents);
                        this.close();
                        childGUI.open();
                    } else {
                        try {
                            if(node instanceof ArgumentCommandNode<?, ?> argNode) {
                                // this node requires argument after it
                                AnvilInputGui inputGui = new AnvilInputGui(player, false);
                                inputGui.setTitle(new TextComponent("Enter " + argNode.getName()));

                                GuiElement confirm = new GuiElement(YES_BUTTON, (index1, type1, action) -> {
                                    String arg = inputGui.getInput();
                                    //command.run(context);
                                    // we fake the argument
                                    StringBuilder builder = new StringBuilder();
                                    parentNodes.forEach(nd -> {
                                        builder.append(nd.getName()).append(" ");
                                    });
                                    builder.append(arg);
                                    player.getServer().getCommands().performCommand(player.createCommandSourceStack(), builder.toString());
                                    player.closeContainer();
                                });

                                GuiElement cancel = new GuiElement(NO_BUTTON, (index1, type1, action) -> {
                                    player.closeContainer();
                                });

                                inputGui.setSlot(2, confirm);
                                inputGui.setSlot(1, cancel);

                                inputGui.setDefaultInputValue(argNode.getExamples().stream().findFirst().get());
                                inputGui.open();
                            } else {
                                player.closeContainer();
                                if(command != null)
                                    command.run(context);
                            }
                        } catch(CommandSyntaxException | IllegalArgumentException e) {
                            player.sendMessage(new TextComponent(e.getMessage()), player.getUUID());
                        }
                    }
                } else if(clickType == ClickType.MOUSE_RIGHT) {
                    try {
                        player.closeContainer();
                        if(command != null)
                            command.run(context);
                    } catch(CommandSyntaxException | IllegalArgumentException e) {
                        player.sendMessage(new TextComponent(e.getMessage()), player.getUUID());
                    }
                }
                this.setSlot(index, item);
            }));
        });
    }

    static {
        YES_BUTTON.setHoverName(new TextComponent("Confirm"));
        NO_BUTTON.setHoverName(new TextComponent("Cancel"));
    }
}
