/*
 * Copyright (C) 2026  literal
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gg.literal.jumpboostbar.fabric;

import gg.literal.jumpboostbar.common.JumpChargeHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class FabricJumpChargeHandler implements JumpChargeHandler {
    private boolean charging = false;
    private int chargeTicks;
    private int currentTicks = 0;
    private Consumer<Float> onProgress;
    private int originalLevel;
    private float originalExp;

    public FabricJumpChargeHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    @Override
    public void startCharging(int chargeTicks, Consumer<Float> onProgress) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.onGround() || !hasJumpBoost(player)) {
            return;
        }

        this.charging = true;
        this.chargeTicks = chargeTicks;
        this.onProgress = onProgress;
        this.currentTicks = 0;
        this.originalLevel = player.experienceLevel;
        this.originalExp = player.experienceProgress;
    }

    @Override
    public void stopCharging() {
        stopCharging(true);
    }

    @Override
    public boolean isCharging() {
        return this.charging;
    }

    private void onClientTick(Minecraft client) {
        if (charging) {
            LocalPlayer player = client.player;
            if (player == null) {
                cancelCharging();
                return;
            }

            if (!player.onGround()) {
                cancelCharging();
                return;
            }

            if (!hasJumpBoost(player)) {
                cancelCharging();
                return;
            }

            if (!player.isShiftKeyDown()) {
                stopCharging();
                return;
            }

            currentTicks++;
            if (onProgress != null) {
                onProgress.accept(getProgress());
            }
        }
    }

    public void cancelCharging() {
        stopCharging(false);
    }

    private void stopCharging(boolean launch) {
        if (!this.charging) {
            return;
        }

        this.charging = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (launch && player.onGround() && hasJumpBoost(player)) {
            double jumpHeight = calculateJumpHeight(player);
            if (jumpHeight > 0) {
                float progress = getProgress();
                double y = Math.sqrt((jumpHeight * progress) * 0.16);
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x, y, velocity.z);
            }
        }

        player.experienceLevel = originalLevel;
        player.experienceProgress = originalExp;
    }

    private boolean hasJumpBoost(LocalPlayer player) {
        return findJumpBoostEffect(player) != null;
    }

    private double calculateJumpHeight(LocalPlayer player) {
        MobEffectInstance instance = findJumpBoostEffect(player);
        if (instance == null) {
            return 0.0;
        }
        int amplifier = instance.getAmplifier() + 1;
        return 1.25 + (amplifier * 1.25);
    }

    private MobEffectInstance findJumpBoostEffect(LocalPlayer player) {
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if ("effect.minecraft.jump_boost".equals(effect.getDescriptionId())) {
                return effect;
            }
        }
        return null;
    }

    private float getProgress() {
        if (chargeTicks <= 0) {
            return 0.0f;
        }

        if (currentTicks <= chargeTicks) {
            return Math.min(1.0f, (float) currentTicks / chargeTicks);
        }

        int oscillationTicks = currentTicks - chargeTicks;
        int cycleLength = chargeTicks * 2;
        int cyclePos = oscillationTicks % cycleLength;
        if (cyclePos < chargeTicks) {
            return 1.0f - ((float) cyclePos / chargeTicks);
        }

        return (float) (cyclePos - chargeTicks) / chargeTicks;
    }
}
