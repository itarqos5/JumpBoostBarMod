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

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("jumpboostbar")
public class JumpBoostBarNeoForge {
    private static final int CHARGE_TICKS = 20;

    public static JumpBoostBarConfig config;
    private final NeoForgeJumpChargeHandler chargeHandler = new NeoForgeJumpChargeHandler();

    public JumpBoostBarNeoForge(IEventBus modEventBus) {
        config = JumpBoostBarConfig.load();
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
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

    private double calculateJumpHeight(LocalPlayer player) {
        MobEffectInstance instance = findJumpBoostEffect(player);
        if (instance == null) {
            return 0.0;
        }
        int amplifier = instance.getAmplifier() + 1;
        return 1.25 + (amplifier * 1.25);
    }

    private boolean hasJumpBoost(LocalPlayer player) {
        return findJumpBoostEffect(player) != null;
    }

    private MobEffectInstance findJumpBoostEffect(LocalPlayer player) {
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if ("effect.minecraft.jump_boost".equals(effect.getDescriptionId())) {
                return effect;
            }
        }
        return null;
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            net.minecraft.commands.Commands.literal("jumpboostbar")
                .then(net.minecraft.commands.Commands.literal("on").executes(context -> {
                    config.setEnabled(true);
                    context.getSource().sendSuccess(() -> Component.literal("§aJumpBoostBar enabled."), false);
                    return 1;
                }))
                .then(net.minecraft.commands.Commands.literal("off").executes(context -> {
                    config.setEnabled(false);
                    chargeHandler.cancelCharging();
                    context.getSource().sendSuccess(() -> Component.literal("§cJumpBoostBar disabled."), false);
                    return 1;
                }))
                .then(net.minecraft.commands.Commands.literal("bar")
                    .then(net.minecraft.commands.Commands.argument("mode", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("xp");
                            builder.suggest("bossbar");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String mode = StringArgumentType.getString(context, "mode");
                            if (!mode.equalsIgnoreCase("xp") && !mode.equalsIgnoreCase("bossbar")) {
                                context.getSource().sendFailure(Component.literal("§cUsage: /jumpboostbar bar <xp|bossbar>"));
                                return 0;
                            }
                            config.setBarMode(BarMode.fromConfig(mode));
                            chargeHandler.cancelCharging();
                            context.getSource().sendSuccess(() -> Component.literal("§aJumpBoostBar bar set to §f" + config.barMode().configValue() + "§a."), false);
                            return 1;
                        })))
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("§e/jumpboostbar on"), false);
                    context.getSource().sendSuccess(() -> Component.literal("§e/jumpboostbar off"), false);
                    context.getSource().sendSuccess(() -> Component.literal("§e/jumpboostbar bar <xp|bossbar>"), false);
                    return 1;
                })
        );

        event.getDispatcher().register(
            net.minecraft.commands.Commands.literal("jbb")
                .redirect(event.getDispatcher().getRoot().getChild("jumpboostbar"))
        );
    }
}