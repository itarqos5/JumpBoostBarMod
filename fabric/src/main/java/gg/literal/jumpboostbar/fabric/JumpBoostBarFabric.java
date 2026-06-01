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

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

public class JumpBoostBarFabric implements ClientModInitializer {
    private static final int CHARGE_TICKS = 20;

    public static JumpBoostBarConfig config;
    private final FabricJumpChargeHandler chargeHandler = new FabricJumpChargeHandler();

    @Override
    public void onInitializeClient() {
        config = JumpBoostBarConfig.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            chargeHandler.cancelCharging();
            return;
        }

        if (!config.isEnabled()) {
            chargeHandler.cancelCharging();
            return;
        }

        if (!hasJumpBoost(player)) {
            chargeHandler.cancelCharging();
            return;
        }

        if (player.isShiftKeyDown() && player.onGround()) {
            if (!chargeHandler.isCharging()) {
                chargeHandler.startCharging(CHARGE_TICKS, progress -> showBars(player, progress));
            }
        } else {
            if (chargeHandler.isCharging()) {
                chargeHandler.stopCharging();
            }
        }
    }

    private void showBars(LocalPlayer player, float progress) {
        double blocks = calculateJumpHeight(player) * Math.min(1.0f, progress);
        String actionbar = config.actionbarMessage().replace("{blocks}", String.format("%.1f", blocks));
        player.displayClientMessage(Component.literal(actionbar.replace('&', '§')), true);

        if (config.barMode() == BarMode.XP || config.barMode() == BarMode.BOSSBAR) {
            player.experienceLevel = (int) Math.round(blocks);
            player.experienceProgress = Math.min(1.0f, progress);
        }
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

    private void registerCommands(com.mojang.brigadier.CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("jumpboostbar")
                .then(ClientCommandManager.literal("on").executes(context -> {
                    config.setEnabled(true);
                    context.getSource().sendFeedback(Component.literal("§aJumpBoostBar enabled."));
                    return 1;
                }))
                .then(ClientCommandManager.literal("off").executes(context -> {
                    config.setEnabled(false);
                    chargeHandler.cancelCharging();
                    context.getSource().sendFeedback(Component.literal("§cJumpBoostBar disabled."));
                    return 1;
                }))
                .then(ClientCommandManager.literal("bar")
                    .then(ClientCommandManager.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("xp");
                            builder.suggest("bossbar");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String mode = StringArgumentType.getString(context, "mode");
                            if (!mode.equalsIgnoreCase("xp") && !mode.equalsIgnoreCase("bossbar")) {
                                context.getSource().sendFeedback(Component.literal("§cUsage: /jumpboostbar bar <xp|bossbar>"));
                                return 0;
                            }
                            config.setBarMode(BarMode.fromConfig(mode));
                            chargeHandler.cancelCharging();
                            context.getSource().sendFeedback(Component.literal("§aJumpBoostBar bar set to §f" + config.barMode().configValue() + "§a."));
                            return 1;
                        })))
                .executes(context -> {
                    context.getSource().sendFeedback(Component.literal("§e/jumpboostbar on"));
                    context.getSource().sendFeedback(Component.literal("§e/jumpboostbar off"));
                    context.getSource().sendFeedback(Component.literal("§e/jumpboostbar bar <xp|bossbar>"));
                    return 1;
                })
        );

        dispatcher.register(ClientCommandManager.literal("jbb").redirect(dispatcher.getRoot().getChild("jumpboostbar")));
    }
}
