package gg.literal.jumpboostbar.fabric;

import gg.literal.jumpboostbar.common.JumpChargeHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class FabricJumpChargeHandler implements JumpChargeHandler {
    private boolean charging = false;
    private int chargeTicks;
    private int currentTicks = 0;
    private Consumer<Float> onProgress;

    public FabricJumpChargeHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    @Override
    public void startCharging(int chargeTicks, Consumer<Float> onProgress) {
        this.charging = true;
        this.chargeTicks = chargeTicks;
        this.onProgress = onProgress;
        this.currentTicks = 0;
    }

    @Override
    public void stopCharging() {
        if (this.charging) {
            this.charging = false;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.onGround()) {
                double jumpHeight = calculateJumpHeight(player);
                if (jumpHeight > 0) {
                    float progress = (float) currentTicks / chargeTicks;
                    double y = Math.sqrt(jumpHeight * progress * 0.16);
                    Vec3 velocity = player.getDeltaMovement();
                    player.setDeltaMovement(velocity.x, y, velocity.z);
                }
            }
        }
    }

    @Override
    public boolean isCharging() {
        return this.charging;
    }

    private void onClientTick(Minecraft client) {
        if (charging) {
            LocalPlayer player = client.player;
            if (player == null || !player.isShiftKeyDown()) {
                stopCharging();
                return;
            }

            currentTicks++;
            if (onProgress != null) {
                onProgress.accept((float) currentTicks / chargeTicks);
            }
        }
    }

    private double calculateJumpHeight(LocalPlayer player) {
        if (player.hasEffect(MobEffects.JUMP)) {
            int amplifier = player.getEffect(MobEffects.JUMP).getAmplifier() + 1;
            return 1.25 + (amplifier * 1.25);
        }
        return 0.0;
    }
}
