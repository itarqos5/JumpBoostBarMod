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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("jumpboostbar")
public class JumpBoostBarNeoForge {
    public static JumpBoostBarConfig config;
    private final JumpChargeHandler chargeHandler = new NeoForgeJumpChargeHandler();

    public JumpBoostBarNeoForge(IEventBus modEventBus) {
        config = JumpBoostBarConfig.load();
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
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
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP_BOOST)) {
            int amplifier = player.getEffect(net.minecraft.world.effect.MobEffects.JUMP_BOOST).getAmplifier() + 1;
            return 1.25 + (amplifier * 1.25);
        }
        return 0.0;
    }
}
