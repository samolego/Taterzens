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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.samo_lego.taterzens.Taterzens.config;

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
        final CompoundTag customData = new CompoundTag();
        customData.putInt("CustomModelData", config.guiItemModelData);

        YES_BUTTON.setHoverName(new TranslatableComponent("gui.done"));
        NO_BUTTON.setHoverName(new TranslatableComponent("gui.cancel"));

        ItemStack create = new ItemStack(Items.PLAYER_HEAD);
        create.setTag(customData.copy());
        itemCommandMap.put("create", create);

        ItemStack select = new ItemStack(Items.SPECTRAL_ARROW);
        select.setTag(customData.copy());
        itemCommandMap.put("select", select);

        ItemStack deselect = new ItemStack(Items.ARROW);
        deselect.setTag(customData.copy());
        itemCommandMap.put("deselect", deselect);

        ItemStack list = new ItemStack(Items.PAPER);
        list.setTag(customData.copy());
        itemCommandMap.put("list", list);

        ItemStack remove = new ItemStack(Items.BARRIER);
        remove.setTag(customData.copy());
        itemCommandMap.put("remove", remove);

        // Edit
        ItemStack edit = new ItemStack(Items.TRIDENT);
        edit.setTag(customData.copy());
        itemCommandMap.put("edit", edit);

        ItemStack behaviour = new ItemStack(Items.CREEPER_HEAD);
        behaviour.setTag(customData.copy());
        itemCommandMap.put("behaviour", behaviour);

        ItemStack commands = new ItemStack(Items.COMMAND_BLOCK);
        commands.setTag(customData.copy());
        itemCommandMap.put("commands", commands);

        ItemStack equipment = new ItemStack(Items.IRON_CHESTPLATE);
        equipment.setTag(customData.copy());
        itemCommandMap.put("equipment", equipment);

        ItemStack messages = new ItemStack(Items.WRITABLE_BOOK);
        messages.setTag(customData.copy());
        itemCommandMap.put("messages", messages);

        ItemStack movement = new ItemStack(Items.MINECART);
        movement.setTag(customData.copy());
        itemCommandMap.put("movement", movement);

        ItemStack look = new ItemStack(Items.ENDER_EYE);
        look.setTag(customData.copy());
        itemCommandMap.put("look", look);

        ItemStack name = new ItemStack(Items.NAME_TAG);
        name.setTag(customData.copy());
        itemCommandMap.put("name", name);

        ItemStack path = new ItemStack(Items.POWERED_RAIL);
        path.setTag(customData.copy());
        itemCommandMap.put("path", path);


        ItemStack pose = new ItemStack(Items.ARMOR_STAND);
        pose.setTag(customData.copy());
        itemCommandMap.put("pose", pose);

        ItemStack mount = new ItemStack(Items.SADDLE);
        mount.setTag(customData.copy());
        itemCommandMap.put("mount", mount);

        ItemStack professions = new ItemStack(Items.DIAMOND_PICKAXE);
        professions.setTag(customData.copy());
        itemCommandMap.put("professions", professions);

        ItemStack skin = new ItemStack(Items.PLAYER_HEAD);
        skin.setTag(customData.copy());
        itemCommandMap.put("skin", skin);

        ItemStack tags = new ItemStack(Items.GLOW_ITEM_FRAME);
        tags.setTag(customData.copy());
        itemCommandMap.put("tags", tags);

        ItemStack type = new ItemStack(Items.SHEEP_SPAWN_EGG);
        type.setTag(customData.copy());
        itemCommandMap.put("type", tags);

        // Messages
        ItemStack messageId = new ItemStack(Items.KNOWLEDGE_BOOK);
        messageId.setTag(customData.copy());
        itemCommandMap.put("message id", messageId);
        itemCommandMap.put("clear", new ItemStack(Items.LAVA_BUCKET));


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
