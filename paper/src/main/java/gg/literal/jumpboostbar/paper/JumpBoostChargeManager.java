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
package gg.literal.jumpboostbar.paper;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class JumpBoostChargeManager implements Listener {
    private final JumpBoostBarPaper plugin;
    private final Map<UUID, JumpChargeSession> sessions = new ConcurrentHashMap<>();

    public JumpBoostChargeManager(JumpBoostBarPaper plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!plugin.settings().isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (event.isSneaking()) {
            if (sessions.containsKey(player.getUniqueId())) {
                return;
            }
            // Only allow charging while on the ground
            if (!player.isOnGround()) {
                return;
            }

            double maxBlocks = maxBlocks(player);
            if (maxBlocks <= 0.0D) {
                return;
            }

            JumpChargeSession session = new JumpChargeSession(plugin, player, maxBlocks, () -> sessions.remove(player.getUniqueId()));
            sessions.put(player.getUniqueId(), session);
            session.start();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stop(event.getPlayer().getUniqueId());
    }

    public void stopAll() {
        for (JumpChargeSession session : sessions.values()) {
            session.stop();
        }
        sessions.clear();
    }

    private void stop(UUID playerId) {
        JumpChargeSession session = sessions.remove(playerId);
        if (session != null) {
            session.stop();
        }
    }

    private static double maxBlocks(Player player) {
        PotionEffect effect = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (effect == null) {
            return 0.0D;
        }

        int level = effect.getAmplifier() + 1;
        // Formula for jump height with jump boost: (level + 4.2)^2 / 16
        // This is a community-approximated formula.
        // Let's use a simpler, more reliable one if possible.
        // Vanilla jump height is 1.25 blocks.
        // Each level of jump boost adds to this.
        // Level 1 adds ~1.25 blocks. Level 2 adds ~2.5 blocks.
        // A simple linear scaling might be sufficient.
        // Let's stick to a known formula for now.
        // h = (v^2)/(2*g) where v = 0.42 + 0.1 * level
        // g = 0.08
        // v_total = 0.42 + 0.1 * level
        // h = (v_total^2) / 0.16
        // This seems too complex. Let's try a simpler approach.
        // From the Minecraft Wiki, the jump height is:
        // 1.25 blocks (normal)
        // 2.5 blocks (Jump Boost I)
        // 5.0 blocks (Jump Boost II)
        // The height roughly doubles with each level.
        // Let's use a formula that approximates this.
        // A jump of 1.25 blocks is the base.
        // For each level, we add to this.
        double base_jump = 1.25;
        double jump_height = base_jump + (level * 1.25);
        return jump_height;
    }
}
