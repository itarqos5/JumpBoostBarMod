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
        // These values are based on the jump height for each level of the jump boost effect.
        // Level 1: 2.5 blocks
        // Level 2: 5 blocks
        // This can be customized for different gameplay experiences.
        if (level == 1) {
            return 2.5D;
        }
        if (level == 2) {
            return 5.0D;
        }

        return 0.0D;
    }
}
