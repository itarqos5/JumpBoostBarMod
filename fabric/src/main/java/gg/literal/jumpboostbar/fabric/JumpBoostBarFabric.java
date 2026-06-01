package gg.literal.jumpboostbar.fabric;

import gg.literal.jumpboostbar.common.JumpChargeHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public class JumpBoostBarFabric implements ClientModInitializer {
    private final JumpChargeHandler chargeHandler = new FabricJumpChargeHandler();

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        if (player.isShiftKeyDown()) {
            if (!chargeHandler.isCharging()) {
                chargeHandler.startCharging(20, progress -> {
                    player.experienceLevel = (int) Math.round(calculateJumpHeight(player) * progress);
                    player.experienceProgress = progress;
                });
            }
        } else {
            if (chargeHandler.isCharging()) {
                chargeHandler.stopCharging();
                player.experienceLevel = 0;
                player.experienceProgress = 0;
            }
        }
    }

    private double calculateJumpHeight(LocalPlayer player) {
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) {
            int amplifier = player.getEffect(net.minecraft.world.effect.MobEffects.JUMP).getAmplifier() + 1;
            return 1.25 + (amplifier * 1.25);
        }
        return 0.0;
    }
}
