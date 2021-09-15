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
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EditorGUI extends SimpleGui {

    private static final ItemStack YES_BUTTON = new ItemStack(Items.GREEN_STAINED_GLASS_PANE);
    private static final ItemStack NO_BUTTON = new ItemStack(Items.RED_STAINED_GLASS_PANE);
    private static final HashMap<String, ItemStack> itemCommandMap = new HashMap<>();

    public EditorGUI(CommandContext<CommandSourceStack> context, ServerPlayer player, EditorGUI previousScreen, List<CommandNode<CommandSourceStack>> parentNodes) {
        super(MenuType.GENERIC_9x6, player, true);
        CommandNode<CommandSourceStack> parentNode = parentNodes.get(parentNodes.size() - 1);

        this.setTitle(new TextComponent(parentNodes.stream().map(n -> n.getName() + " ").collect(Collectors.joining())));
        this.setAutoUpdate(true);


        ItemStack back = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
        back.setHoverName(new TextComponent("Back"));

        GuiElement backScreenButton = new GuiElement(back, (i, clickType, slotActionType) -> {
            if (previousScreen == null) {
                player.closeContainer();
            } else
                previousScreen.open();
        });
        this.setSlot(0, backScreenButton);

        ItemStack close = new ItemStack(Items.STRUCTURE_VOID);
        close.setHoverName(new TextComponent("Close"));


        GuiElement closeScreenButton = new GuiElement(close, (i, clickType, slotActionType) -> {
            player.closeContainer();
        });
        this.setSlot(8, closeScreenButton);

        AtomicInteger i = new AtomicInteger(10);

        // Looping through command node
        for (CommandNode<CommandSourceStack> node : parentNode.getChildren()) {
            // Tracking current command "path"
            ArrayList<CommandNode<CommandSourceStack>> parents = new ArrayList<>(parentNodes);
            parents.add(node);

            ItemStack stack = itemCommandMap.getOrDefault(node.getName(), new ItemStack(Items.ITEM_FRAME));
            stack.setHoverName(new TextComponent(node.getName()));


            this.setSlot(i.getAndAdd(3), new GuiElement(stack, (index, clickType, slotActionType) -> {
                Command<CommandSourceStack> command = node.getCommand();

                if (clickType == ClickType.MOUSE_LEFT) {
                    // Show children or execute if no children
                    Collection<CommandNode<CommandSourceStack>> children = node.getChildren();

                    if (children.size() > 1 && !(node instanceof ArgumentCommandNode<?, ?>)) {
                        EditorGUI childGUI = new EditorGUI(context, player, this, parents);
                        this.close();
                        childGUI.open();
                    } else {
                        CommandNode<CommandSourceStack> commandNode = node;

                        // If there's one child which is an argument type, skip to it
                        if (children.size() == 1 && !(commandNode instanceof ArgumentCommandNode<?, ?>)) {
                            commandNode = (CommandNode<CommandSourceStack>) children.toArray()[0];
                        }
                        try {
                            if (commandNode instanceof ArgumentCommandNode<?, ?> argNode) {
                                // this node requires argument after it
                                AnvilInputGui inputGui = new AnvilInputGui(player, false);
                                inputGui.setTitle(new TextComponent(argNode.getName()));

                                GuiElement confirmButton = new GuiElement(YES_BUTTON, (index1, type1, action) -> {
                                    String arg = inputGui.getInput();

                                    // we "fake" the command
                                    StringBuilder builder = new StringBuilder();
                                    parents.forEach(nd -> {
                                        if(!(nd instanceof ArgumentCommandNode<?, ?>))
                                            builder.append(nd.getName()).append(" ");
                                    });
                                    builder.append(arg);

                                    player.getServer().getCommands().performCommand(player.createCommandSourceStack(), builder.toString());
                                    player.closeContainer();
                                });

                                GuiElement cancelButton = new GuiElement(NO_BUTTON, (index1, type1, action) -> {
                                    player.closeContainer();
                                });

                                inputGui.setSlot(2, confirmButton);
                                inputGui.setSlot(1, cancelButton);

                                // Default input value
                                Optional<String> example = argNode.getExamples().stream().findFirst();
                                example.ifPresent(inputGui::setDefaultInputValue);

                                inputGui.open();
                            } else {
                                player.closeContainer();
                                if (command != null)
                                    command.run(context);
                            }
                        } catch (CommandSyntaxException | IllegalArgumentException e) {
                            player.sendMessage(new TextComponent(e.getMessage()), player.getUUID());
                        }
                    }
                } else if (clickType == ClickType.MOUSE_RIGHT) {
                    // Execute
                    try {
                        player.closeContainer();
                        if (command != null)
                            command.run(context);
                    } catch (CommandSyntaxException | IllegalArgumentException e) {
                        player.sendMessage(new TextComponent(e.getMessage()), player.getUUID());
                    }
                }
            }));
        }
    }

    static {
        YES_BUTTON.setHoverName(new TextComponent("Confirm"));
        NO_BUTTON.setHoverName(new TextComponent("Cancel"));

        itemCommandMap.put("create", new ItemStack(Items.PLAYER_HEAD));
        itemCommandMap.put("select", new ItemStack(Items.SPECTRAL_ARROW));
        itemCommandMap.put("deselect", new ItemStack(Items.ARROW));
        itemCommandMap.put("list", new ItemStack(Items.PAPER));
        itemCommandMap.put("remove", new ItemStack(Items.BARRIER));

        // Edit
        itemCommandMap.put("edit", new ItemStack(Items.TRIDENT));
        itemCommandMap.put("behaviour", new ItemStack(Items.CREEPER_HEAD));
        itemCommandMap.put("commands", new ItemStack(Items.COMMAND_BLOCK));
        itemCommandMap.put("equipment", new ItemStack(Items.IRON_CHESTPLATE));
        itemCommandMap.put("messages", new ItemStack(Items.WRITABLE_BOOK));
        itemCommandMap.put("movement", new ItemStack(Items.MINECART));
        itemCommandMap.put("look", new ItemStack(Items.ENDER_EYE));
        itemCommandMap.put("name", new ItemStack(Items.NAME_TAG));
        itemCommandMap.put("path", new ItemStack(Items.POWERED_RAIL));
        itemCommandMap.put("pose", new ItemStack(Items.SMOOTH_QUARTZ_STAIRS));
        itemCommandMap.put("mount", new ItemStack(Items.SADDLE));
        itemCommandMap.put("professions", new ItemStack(Items.DIAMOND_PICKAXE));
        itemCommandMap.put("skin", new ItemStack(Items.PLAYER_HEAD));
        itemCommandMap.put("tags", new ItemStack(Items.ITEM_FRAME));
        itemCommandMap.put("type", new ItemStack(Items.SHEEP_SPAWN_EGG));

        // Presets
        itemCommandMap.put("preset", new ItemStack(Items.CREEPER_HEAD));
        itemCommandMap.put("save", new ItemStack(Items.CAULDRON));
        itemCommandMap.put("load", new ItemStack(Items.GLOW_SQUID_SPAWN_EGG));


        itemCommandMap.put("tp", new ItemStack(Items.ENDER_PEARL));
        itemCommandMap.put("entity", new ItemStack(Items.ZOMBIE_HEAD));
        itemCommandMap.put("location", new ItemStack(Items.TRIPWIRE_HOOK));


        itemCommandMap.put("action", new ItemStack(Items.CHAIN_COMMAND_BLOCK));
        itemCommandMap.put("goto", new ItemStack(Items.MINECART));
        itemCommandMap.put("interact", new ItemStack(Items.REDSTONE_TORCH));
    }
}
