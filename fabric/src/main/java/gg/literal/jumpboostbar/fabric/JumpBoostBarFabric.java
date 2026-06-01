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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class JumpBoostBarFabric implements ClientModInitializer {
    public static JumpBoostBarConfig config;
    private final JumpChargeHandler chargeHandler = new FabricJumpChargeHandler();

    @Override
    public void onInitializeClient() {
        config = JumpBoostBarConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !config.isEnabled()) {
            return;
        }

        if (player.isShiftKeyDown()) {
            if (!chargeHandler.isCharging()) {
                chargeHandler.startCharging(20, progress -> {
                    player.experienceLevel = (int) Math.round(calculateJumpHeight(player) * progress);
                    player.experienceProgress = Math.min(1.0f, progress);
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
        return player.getActiveEffects().stream()
            .filter(effect -> effect.getEffect().is(net.minecraft.world.effect.BuiltInMobEffects.JUMP_BOOST))
            .findFirst()
            .map(effect -> 1.25 + (effect.getAmplifier() + 1) * 1.25)
            .orElse(0.0);
    }
}
