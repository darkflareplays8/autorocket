package com.example.autorocket;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoRocketMod implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private boolean enabled = true;
    private int cooldown = 0;

    private ItemStack previousOffhand = ItemStack.EMPTY;
    private boolean wasFlying = false;

    @Override
    public void onInitializeClient() {

        AutoConfig.register(AutoRocketConfig.class, GsonConfigSerializer::new);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autorocket.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                client.player.sendMessage(
                        Text.literal("Auto Rocket+: " + (enabled ? "§aON" : "§cOFF")),
                        true
                );
            }

            if (!enabled) return;

            boolean flying = client.player.isGliding();

            // Started flying → save offhand
            if (flying && !wasFlying) {
                previousOffhand = client.player.getOffHandStack().copy();
            }

            // Landed → restore offhand
            if (!flying && wasFlying) {
                restoreOffhand(client);
            }

            wasFlying = flying;

            if (!flying) return;

            if (cooldown > 0) {
                cooldown--;
                return;
            }

            ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
            if (chest.getItem() != Items.ELYTRA) return;

            useRocket(client);
        });
    }

    private void useRocket(MinecraftClient client) {
        if (client.interactionManager == null) return;
        if (!(client.player.currentScreenHandler instanceof PlayerScreenHandler handler)) return;

        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);

            if (stack.getItem() == Items.FIREWORK_ROCKET) {

                int slotId = i < 9 ? i + 36 : i;

                client.interactionManager.clickSlot(
                        handler.syncId, slotId, 0,
                        SlotActionType.PICKUP, client.player
                );

                client.interactionManager.clickSlot(
                        handler.syncId, 45, 0,
                        SlotActionType.PICKUP, client.player
                );

                if (!handler.getCursorStack().isEmpty()) {
                    client.interactionManager.clickSlot(
                            handler.syncId, slotId, 0,
                            SlotActionType.PICKUP, client.player
                    );
                }

                cooldown = 15;
                break;
            }
        }
    }

    private void restoreOffhand(MinecraftClient client) {
        if (client.interactionManager == null) return;
        if (!(client.player.currentScreenHandler instanceof PlayerScreenHandler handler)) return;
        if (previousOffhand.isEmpty()) return;

        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);

            if (ItemStack.areItemsEqual(stack, previousOffhand)) {
                int slotId = i < 9 ? i + 36 : i;

                client.interactionManager.clickSlot(
                        handler.syncId, slotId, 0,
                        SlotActionType.PICKUP, client.player
                );

                client.interactionManager.clickSlot(
                        handler.syncId, 45, 0,
                        SlotActionType.PICKUP, client.player
                );

                if (!handler.getCursorStack().isEmpty()) {
                    client.interactionManager.clickSlot(
                            handler.syncId, slotId, 0,
                            SlotActionType.PICKUP, client.player
                    );
                }
                break;
            }
        }

        previousOffhand = ItemStack.EMPTY;
    }
}
