package org.samo_lego.taterzens.fabric.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.common.Taterzens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.samo_lego.taterzens.common.Taterzens.config;

public class EditorGUI {
    public static final String MODEL_DATA_ID = "taterzens_gui_item";

    private static final ItemStack YES_BUTTON = new ItemStack(Items.GREEN_STAINED_GLASS_PANE);
    private static final ItemStack NO_BUTTON = new ItemStack(Items.RED_STAINED_GLASS_PANE);
    private static final HashMap<String, ItemStack> itemCommandMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static SimpleGui createCommandGui(ServerPlayer player, @Nullable SimpleGui previousScreen, CommandNode<CommandSourceStack> parentNode, List<String> currentCommandPath, boolean givenInput) {
        // If node is not an argument, we skip to first child node that is an argument or has more than 1 child node
        while (parentNode.getChildren().size() == 1 && !(parentNode instanceof ArgumentCommandNode<?, ?>)) {
            CommandNode<CommandSourceStack> childNode = (CommandNode<CommandSourceStack>) parentNode.getChildren().toArray()[0];
            if (childNode instanceof ArgumentCommandNode) {
                givenInput = false;
            } else if (!childNode.getChildren().isEmpty()) {
                currentCommandPath.add(parentNode.getName());
            } else {
                break;
            }
            parentNode = childNode;

        }

        var childNodes = parentNode.getChildren();
        boolean argumentNode = parentNode instanceof ArgumentCommandNode<?, ?>;

        SimpleGui constructedGui;
        if(argumentNode && !givenInput) {
            constructedGui = new AnvilInputGui(player, true);

            final CommandNode<CommandSourceStack> finalParentNode = parentNode;
            GuiElement confirmButton = new GuiElement(YES_BUTTON, (index, clickType, slotActionType) -> {
                String arg = ((AnvilInputGui) constructedGui).getInput();
                // We "set" the argument to overwrite parent node (arg name)
                currentCommandPath.add(arg);

                CommandNode<CommandSourceStack> newNode = finalParentNode;
                if (childNodes.size() == 1)
                    newNode = (CommandNode<CommandSourceStack>) childNodes.toArray()[0];

                proccessClick(clickType, newNode, constructedGui, currentCommandPath, player, childNodes.size() != 1);
            });

            // Pre-written  text
            MutableComponent argTitle = Component.literal(parentNode.getName()).withStyle(ChatFormatting.YELLOW);
            ItemStack nameStack = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE);  // invisible (kinda)
            nameStack.set(DataComponents.CUSTOM_NAME, getComandName(argTitle.getString()));

            // Buttons
            constructedGui.setSlot(2, confirmButton);
            constructedGui.setSlot(0, nameStack);

            // Default input value
            String[] examples = parentNode.getExamples().toArray(new String[0]);
            if (examples.length > 0) {
                nameStack.set(DataComponents.CUSTOM_NAME, Component.literal(examples[0]));
            }

            for (int i = 1; i < examples.length; ++i) {
                ItemStack exampleStack = new ItemStack(Items.PAPER);
                String example = examples[i];
                exampleStack.set(DataComponents.CUSTOM_NAME, Component.literal(example));

                // 2 being the last slot index in anvil inventory
                constructedGui.setSlot(i * 2 + 1, new GuiElement(exampleStack, (index, type, action) -> {
                    String input = ((AnvilInputGui) constructedGui).getInput();
                    ((AnvilInputGui) constructedGui).setDefaultInputValue(exampleStack.getHoverName().getString());
                    exampleStack.set(DataComponents.CUSTOM_NAME, Component.literal(input));
                }));
            }
        } else {
            // Creates the biggest possible container
            constructedGui = new SimpleGui(MenuType.GENERIC_9x6, player, true);

            // Close screen button
            ItemStack close = new ItemStack(Items.STRUCTURE_VOID);
            close.set(DataComponents.CUSTOM_NAME, Component.translatable("spectatorMenu.close"));
            close.enchant(null, 0);

            GuiElement closeScreenButton = new GuiElement(close, (i, clickType, slotActionType) -> player.closeContainer());
            constructedGui.setSlot(8, closeScreenButton);

            // Integer to track item positions
            AtomicInteger i = new AtomicInteger(10);

            // Looping through command node, 8 * 9 being the available inventory space
            int addedSpace = (childNodes.size() < 8 * 9 / 3) ? (3) : (8 * 9 / childNodes.size());
            for (CommandNode<CommandSourceStack> node : childNodes) {
                // Tracking current command "path"
                // after each menu is opened, we add a node to queue
                ArrayList<String> parents = new ArrayList<>(currentCommandPath);
                String nodeName = node.getName();
                if(!(node instanceof ArgumentCommandNode<?, ?>))
                    parents.add(nodeName);

                // Set stack "icon"
                ItemStack stack = itemCommandMap.getOrDefault(nodeName, new ItemStack(ListItemsGUI.getFromName(nodeName))).copy();
                stack.set(DataComponents.CUSTOM_NAME, getComandName(nodeName));

                // Recursively adding the command nodes
                constructedGui.setSlot(i.getAndAdd(addedSpace), new GuiElement(stack, (index, clickType, slotActionType) -> {
                    // Different action happens on right or left click
                    proccessClick(clickType, node, constructedGui, parents, player, false);
                }));
            }
        }

        // Back button
        ItemStack back = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
        back.set(DataComponents.CUSTOM_NAME, Component.translatable("gui.back"));
        back.enchant(null, 0);

        GuiElement backScreenButton = new GuiElement(back, (i, clickType, slotActionType) -> {
            if (previousScreen == null) {
                player.closeContainer();
            } else {
                constructedGui.close();
                previousScreen.open();
            }
        });
        constructedGui.setSlot(argumentNode && !givenInput ? 1 : 0, backScreenButton);

        // GUI Title - each node adds to it
        var title = new StringBuilder();
        currentCommandPath.forEach(s -> title.append(s).append(" "));
        var textTitle = Component.literal(title.toString());

        constructedGui.setTitle(textTitle.withStyle(ChatFormatting.YELLOW));
        constructedGui.setAutoUpdate(true);

        return constructedGui;
    }

    /**
     * Gets prettier name for command node if available.
     *
     * @param defaultText default name
     * @return "translated" name if available, default name otherwise
     */
    private static Component getComandName(String defaultText) {
        // Try to search for the command translation in the language file
        String text = defaultText;

        if (Taterzens.lang.has("gui.taterzens." + text)) {
            return Component.literal(Taterzens.lang.get("gui.taterzens." + text).getAsString());
        }

        // Upper case first letter
        char[] chars = text.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);

        return Component.literal(new String(chars));
    }

    private static void proccessClick(ClickType clickType, CommandNode<CommandSourceStack> node, SimpleGui gui, List<String> currentCommandPath, ServerPlayer player, boolean givenInput) {
        StringBuilder builder = new StringBuilder();
        // Builds the command from parents
        currentCommandPath.forEach(s -> builder.append(s).append(" "));
        // Delete last space
        builder.deleteCharAt(builder.length() - 1);

        if (config.prefersExecution.contains(builder.toString())) {
            // Swaps the click type
           clickType = clickType == ClickType.MOUSE_LEFT ? ClickType.MOUSE_RIGHT : ClickType.MOUSE_LEFT;
        }

        if (clickType == ClickType.MOUSE_LEFT && node.getChildren().size() > 0 || (node instanceof ArgumentCommandNode<?, ?> && !givenInput)) {
            createCommandGui(player, gui, node, currentCommandPath, givenInput).open();
        } else {
            execute(player, currentCommandPath);
        }
        gui.close();
    }

    /**
     * Executes the command
     * @param player player to execute command as.
     * @param commandTree command tree to execute.
     */
    private static void execute(ServerPlayer player, List<String> commandTree) {
        try {
            // Execute
            // we "fake" the command
            StringBuilder builder = new StringBuilder();

            // Builds the command from commandTree
            commandTree.forEach(nd -> builder.append(nd).append(" "));
            // Delete last space
            builder.deleteCharAt(builder.length() - 1);

            player.closeContainer();

            player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), builder.toString());
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(Component.literal(e.getMessage()));
        }
    }

    static {
        //final CompoundTag customData = new CompoundTag();
        final DataComponentPatch customData = DataComponentPatch.builder()
            .set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(MODEL_DATA_ID), List.of(config.guiItemModelData)))
            // HideFlags has been split up into the individual components for each piece of info it could hide
            // we only set DataCompoenets.ATTRIBUTE_MODIFIERS since (at least with the current items in the gui)
            // that looks identical
            .set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
            .build();

        YES_BUTTON.set(DataComponents.CUSTOM_NAME, Component.translatable("gui.done"));
        NO_BUTTON.set(DataComponents.CUSTOM_NAME, Component.translatable("gui.cancel"));

        ItemStack create = new ItemStack(Items.PLAYER_HEAD);
        create.applyComponents(customData);
        itemCommandMap.put("create", create);

        ItemStack select = new ItemStack(Items.SPECTRAL_ARROW);
        select.applyComponents(customData);
        itemCommandMap.put("select", select);

        ItemStack deselect = new ItemStack(Items.ARROW);
        deselect.applyComponents(customData);
        itemCommandMap.put("deselect", deselect);

        ItemStack list = new ItemStack(Items.PAPER);
        list.applyComponents(customData);
        itemCommandMap.put("list", list);

        ItemStack remove = new ItemStack(Items.BARRIER);
        remove.applyComponents(customData);
        itemCommandMap.put("remove", remove);

        // Edit
        ItemStack edit = new ItemStack(Items.TRIDENT);
        edit.applyComponents(customData);
        itemCommandMap.put("edit", edit);

        ItemStack behaviour = new ItemStack(Items.CREEPER_HEAD);
        behaviour.applyComponents(customData);
        itemCommandMap.put("behaviour", behaviour);

        ItemStack commands = new ItemStack(Items.COMMAND_BLOCK);
        commands.applyComponents(customData);
        itemCommandMap.put("commands", commands);

        ItemStack equipment = new ItemStack(Items.IRON_CHESTPLATE);
        equipment.applyComponents(customData);
        itemCommandMap.put("equipment", equipment);

        ItemStack messages = new ItemStack(Items.WRITABLE_BOOK);
        messages.applyComponents(customData);;
        itemCommandMap.put("messages", messages);

        ItemStack movement = new ItemStack(Items.MINECART);
        movement.applyComponents(customData);
        itemCommandMap.put("movement", movement);

        ItemStack look = new ItemStack(Items.ENDER_EYE);
        look.applyComponents(customData);
        itemCommandMap.put("look", look);

        ItemStack name = new ItemStack(Items.NAME_TAG);
        name.applyComponents(customData);
        itemCommandMap.put("name", name);

        ItemStack path = new ItemStack(Items.POWERED_RAIL);
        path.applyComponents(customData);
        itemCommandMap.put("path", path);


        ItemStack pose = new ItemStack(Items.ARMOR_STAND);
        pose.applyComponents(customData);
        itemCommandMap.put("pose", pose);

        ItemStack mount = new ItemStack(Items.SADDLE);
        mount.applyComponents(customData);
        itemCommandMap.put("mount", mount);

        ItemStack professions = new ItemStack(Items.DIAMOND_PICKAXE);
        professions.applyComponents(customData);
        itemCommandMap.put("professions", professions);

        ItemStack skin = new ItemStack(Items.PLAYER_HEAD);
        skin.applyComponents(customData);
        itemCommandMap.put("skin", skin);

        ItemStack tags = new ItemStack(Items.GLOW_ITEM_FRAME);
        tags.applyComponents(customData);
        itemCommandMap.put("tags", tags);

        ItemStack type = new ItemStack(Items.SHEEP_SPAWN_EGG);
        type.applyComponents(customData);
        itemCommandMap.put("type", type);

        // Messages
        ItemStack messageId = new ItemStack(Items.KNOWLEDGE_BOOK);
        messageId.applyComponents(customData);
        itemCommandMap.put("message id", messageId);


        ItemStack messageSwap = new ItemStack(Items.WEEPING_VINES);
        messageSwap.applyComponents(customData);
        itemCommandMap.put("swap", messageSwap);

        ItemStack clear = new ItemStack(Items.LAVA_BUCKET);
        clear.applyComponents(customData);
        itemCommandMap.put("clear", clear);


        // Presets
        ItemStack preset = new ItemStack(Items.CREEPER_HEAD);
        preset.applyComponents(customData);
        itemCommandMap.put("preset", preset);

        ItemStack save = new ItemStack(Items.CAULDRON);
        save.applyComponents(customData);
        itemCommandMap.put("save", save);

        ItemStack load = new ItemStack(Items.GLOW_SQUID_SPAWN_EGG);
        load.applyComponents(customData);
        itemCommandMap.put("load", load);


        ItemStack tp = new ItemStack(Items.ENDER_PEARL);
        tp.applyComponents(customData);
        itemCommandMap.put("tp", tp);

        ItemStack entity = new ItemStack(Items.ZOMBIE_HEAD);
        entity.applyComponents(customData);
        itemCommandMap.put("entity", entity);

        ItemStack location = new ItemStack(Items.TRIPWIRE_HOOK);
        location.applyComponents(customData);
        itemCommandMap.put("location", location);


        ItemStack action = new ItemStack(Items.CHAIN_COMMAND_BLOCK);
        action.applyComponents(customData);
        itemCommandMap.put("action", action);

        ItemStack goTo = new ItemStack(Items.MINECART);
        goTo.applyComponents(customData);
        itemCommandMap.put("goto", goTo);

        ItemStack interact = new ItemStack(Items.REDSTONE_TORCH);
        interact.applyComponents(customData);
        itemCommandMap.put("interact", interact);

        // Types
        ItemStack player = new ItemStack(Items.PLAYER_HEAD);
        player.applyComponents(customData);
		UUID uid = UUID.randomUUID(); // a fake UUID just to be able to generate GameProfile for Texture use.
        GameProfile texturesProfile = new GameProfile(uid, "taterzen");
        PropertyMap properties = texturesProfile.getProperties();
        properties.put(
            "textures",
            new Property(
                "ewogICJ0aW1lc3RhbXAiIDogMTYwNjIyODAxMzY0NCwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTczNTE0YTIzMjQ1ZjE1ZGJhZDVmYjRlNjIyMTYzMDIwODY0Y2NlNGMxNWQ1NmRlM2FkYjkwZmE1YTcxMzdmZCIKICAgIH0KICB9Cn0=",
                "T4Mifh5Yr/+jjAe6y+Ai7d1BPIWQGXc6vwtDL9GgxvQFYtxeD2VuSMNniLoSkP5koBDyHE9ZLgzE2GGAbBSGFgdEKBK7stUPEaUhCET6NKQGli369my3t4Z/4fTkFd9lJmMjP84xIo33E69umQLRZN6MfxmAFXdAl0fkjBdpVi3zLsTdgyu01PhlF9/P4TMXJmNjeiUDt6IjdHgWN1UVFYfAMr9UnCvBNQ/Z4MzxXEm8lGrhq0u7piZqJZ4hb15vHVfixXwtJQkJSBxyzry2W9ZZ2l4xReYX4LbBxU2mRVY5ylRbbolpDuMjXJ6vcg+hRQ9c5HhKkYLm/GOloYEHF/LA5FjGD0QGPW/+uzPfFc9b9swdTUXrJS18/d0dYUDvnHWacDuSoQDfb9eszvs4p6JW04Kd/fPAjLrHm36itVgmrkGa4+fA0Sd/3qo3JaRN6rkbzvppc9s7T2jrhz2+h+hSiiXdRv7v1vMhHVFaOayzBmckL+aKcq7HEsDg1MMauoA/OzkWekuk4FqbgZz49nylOcCHVfd7X1SO7D1BicTgdvGGTOVZtYCyfMKCxcxXFgcqQe88BcLujYWsWafO+VPer9RykXAStb80L020KA0FsQ3zOIC0SBgGlTH5E2Z66AyBEcevYqfIUu1G6Gq4uWINrMae4ZKAABOhtoWH+1Y="
            )
        );
        player.set(DataComponents.PROFILE, new ResolvableProfile(texturesProfile));

        itemCommandMap.put("minecraft:player", player);
        itemCommandMap.put("player", player);
        itemCommandMap.put("reset", player);
    }
}
