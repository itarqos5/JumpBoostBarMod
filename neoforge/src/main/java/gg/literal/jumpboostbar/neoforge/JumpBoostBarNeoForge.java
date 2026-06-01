package gg.literal.jumpboostbar.neoforge;

import gg.literal.jumpboostbar.common.JumpChargeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("jumpboostbar")
public class JumpBoostBarNeoForge {
    private final JumpChargeHandler chargeHandler = new NeoForgeJumpChargeHandler();

    public JumpBoostBarNeoForge(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
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
