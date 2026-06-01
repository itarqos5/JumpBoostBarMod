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
package gg.literal.jumpboostbar.neoforge;

import gg.literal.jumpboostbar.common.JumpChargeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.function.Consumer;

public class NeoForgeJumpChargeHandler implements JumpChargeHandler {
    private boolean charging = false;
    private int chargeTicks;
    private int currentTicks = 0;
    private Consumer<Float> onProgress;

    public NeoForgeJumpChargeHandler() {
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
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

    private void onClientTick(ClientTickEvent.Post event) {
        if (charging) {
            LocalPlayer player = Minecraft.getInstance().player;
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
        if (player.hasEffect(MobEffects.JUMP_BOOST)) {
            int amplifier = player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1;
            return 1.25 + (amplifier * 1.25);
        }
        return 0.0;
    }
}
