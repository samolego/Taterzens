package org.samo_lego.taterzens.gui;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EditorGUI extends SimpleGui {

    private static final ItemStack YES_BUTTON = new ItemStack(Items.GREEN_STAINED_GLASS_PANE);
    private static final ItemStack NO_BUTTON = new ItemStack(Items.RED_STAINED_GLASS_PANE);
    private static final HashMap<String, ItemStack> itemCommandMap = new HashMap<>();

    public EditorGUI(CommandContext<CommandSourceStack> context, ServerPlayer player, EditorGUI previousScreen, CommandNode<CommandSourceStack> parentNode, List<String> currentCommandPath) {
        // Creates the biggest possible container
        super(MenuType.GENERIC_9x6, player, true);

        // GUI Title - each node adds to it
        String title = currentCommandPath.stream().map(n -> n + " ").collect(Collectors.joining());
        TextComponent textTitle = new TextComponent(title);

        this.setTitle(textTitle.withStyle(ChatFormatting.YELLOW));
        this.setAutoUpdate(true);


        // Back button
        ItemStack back = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
        back.setHoverName(new TranslatableComponent("gui.back"));

        GuiElement backScreenButton = new GuiElement(back, (i, clickType, slotActionType) -> {
            if (previousScreen == null) {
                player.closeContainer();
            } else
                previousScreen.open();
        });
        this.setSlot(0, backScreenButton);

        // Close menu button
        ItemStack close = new ItemStack(Items.STRUCTURE_VOID);
        close.setHoverName(new TranslatableComponent("spectatorMenu.close"));

        GuiElement closeScreenButton = new GuiElement(close, (i, clickType, slotActionType) -> {
            player.closeContainer();
        });
        this.setSlot(8, closeScreenButton);


        // Integer to track item positions
        AtomicInteger i = new AtomicInteger(10);

        // Looping through command node
        for (CommandNode<CommandSourceStack> node : parentNode.getChildren()) {
            // Tracking current command "path"
            // after each menu is opened, we add a node to queue
            ArrayList<String> parents = new ArrayList<>(currentCommandPath);
            parents.add(node.getName());

            // Set stack "icon"
            ItemStack stack = itemCommandMap.getOrDefault(node.getName(), new ItemStack(Items.ITEM_FRAME));
            stack.setHoverName(new TextComponent(node.getName()));

            // Recursively adding the command nodes
            this.setSlot(i.getAndAdd(3), new GuiElement(stack, (index, clickType, slotActionType) -> {
                // Different action happens on right or left click
                if (clickType == ClickType.MOUSE_LEFT) {
                    generateChildNodes(context, player, node, parents, backScreenButton);
                } else if (clickType == ClickType.MOUSE_RIGHT) {
                    execute(player, parents);
                }
            }));
        }
    }


    private void generateChildNodes(CommandContext<CommandSourceStack> context, ServerPlayer player, CommandNode<CommandSourceStack> parentNode, ArrayList<String> currentCommand, GuiElement backScreenButton) {
        Collection<CommandNode<CommandSourceStack>> children = parentNode.getChildren();

        // Has children, navigate to new instance of GUI
        if (children.size() > 1 && !(parentNode instanceof ArgumentCommandNode<?, ?>)) {
            EditorGUI childGUI = new EditorGUI(context, player, this, parentNode, currentCommand);
            this.close();
            childGUI.open();
        } else {  // Is an argument node / node with one child

            boolean skipped = false;
            // If there's one child which is an argument type, skip to it
            if (children.size() == 1 && !(parentNode instanceof ArgumentCommandNode<?, ?>)) {
                skipped = true;
                parentNode = (CommandNode<CommandSourceStack>) children.toArray()[0];
            }
            if (parentNode instanceof ArgumentCommandNode<?, ?> argNode) {
                // this node requires argument after it, we use anvil gui
                AnvilInputGui inputGui = new AnvilInputGui(player, false);

                CommandNode<CommandSourceStack> finalCommandNode = parentNode;
                boolean finalSkipped = skipped;
                GuiElement confirmButton = new GuiElement(YES_BUTTON, (index, clickType, slotActionType) -> {
                    String arg = inputGui.getInput();

                    if(clickType.equals(ClickType.MOUSE_LEFT) && finalCommandNode.getChildren().size() > 0) {
                        currentCommand.set(currentCommand.size() - 1, arg);
                        EditorGUI childGUI = new EditorGUI(context, player, this, finalCommandNode, currentCommand);
                        this.close();
                        childGUI.open();
                    } else {
                        if(finalSkipped) {
                            currentCommand.add(arg);
                        } else {
                            currentCommand.set(currentCommand.size() - 1, arg);
                        }
                        execute(player, currentCommand);
                    }
                });

                // Pre-written  text
                TextComponent argGui = new TextComponent(inputGui.getInput());
                MutableComponent argTitle = new TextComponent(argNode.getName()).withStyle(ChatFormatting.YELLOW);
                ItemStack nameStack = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE);  // invisible (kinda)
                nameStack.setHoverName(argGui.append(argTitle));

                GuiElement name = new GuiElement(nameStack, (index1, type1, action) -> {
                });

                // Buttons
                inputGui.setSlot(2, confirmButton);
                inputGui.setSlot(1, backScreenButton);
                inputGui.setSlot(0, name);

                // Default input value
                Optional<String> example = argNode.getExamples().stream().findFirst();
                example.ifPresent(s -> nameStack.setHoverName(new TextComponent(s)));

                inputGui.open();
            } else {
                execute(player, currentCommand);
            }
        }
    }

    private void execute(ServerPlayer player, ArrayList<String> parents) {
        try {
            // Execute
            // we "fake" the command
            StringBuilder builder = new StringBuilder();

            // Builds the command from parents
            parents.forEach(nd -> builder.append(nd).append(" "));
            builder.deleteCharAt(builder.length() - 1);

            player.getServer().getCommands().performCommand(player.createCommandSourceStack(), builder.toString());
            player.closeContainer();
            this.onClose();

        } catch (IllegalArgumentException e) {
            player.sendMessage(new TextComponent(e.getMessage()), player.getUUID());
        }
    }


static {
        YES_BUTTON.setHoverName(new TranslatableComponent("gui.done"));
        NO_BUTTON.setHoverName(new TranslatableComponent("gui.cancel"));

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
        itemCommandMap.put("pose", new ItemStack(Items.ARMOR_STAND));
        itemCommandMap.put("mount", new ItemStack(Items.SADDLE));
        itemCommandMap.put("professions", new ItemStack(Items.DIAMOND_PICKAXE));
        itemCommandMap.put("skin", new ItemStack(Items.PLAYER_HEAD));
        itemCommandMap.put("tags", new ItemStack(Items.ITEM_FRAME));
        itemCommandMap.put("type", new ItemStack(Items.SHEEP_SPAWN_EGG));

        // Messages
        itemCommandMap.put("message id", new ItemStack(Items.KNOWLEDGE_BOOK));
        itemCommandMap.put("clear", new ItemStack(Blocks.LAVA));


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

        // Types
        itemCommandMap.put("minecraft:player", new ItemStack(Items.PLAYER_HEAD));
        itemCommandMap.put("player", new ItemStack(Items.PLAYER_HEAD));
        itemCommandMap.put("reset", new ItemStack(Items.PLAYER_HEAD));
    }
}
