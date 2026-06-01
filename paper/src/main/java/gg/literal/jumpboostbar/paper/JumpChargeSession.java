package gg.literal.jumpboostbar.paper;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class JumpChargeSession {
    private static final int CHARGE_TICKS = 20; // Reduced for quicker jumps

    private final JumpBoostBarPaper plugin;
    private final Player player;
    private final double maxBlocks;
    private final Runnable onComplete;

    private ScheduledTask task;
    private BossBar bossBar;
    private int tick;
    private final int originalLevel;
    private final float originalExp;

    public JumpChargeSession(JumpBoostBarPaper plugin, Player player, double maxBlocks, Runnable onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.maxBlocks = maxBlocks;
        this.onComplete = onComplete;
        this.originalLevel = player.getLevel();
        this.originalExp = player.getExp();
    }

    public void start() {
        if (plugin.settings().barMode() == BarMode.BOSSBAR) {
            bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);
        }

        task = player.getScheduler().runAtFixedRate(plugin, this::tick, this::retired, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        cleanup();
    }

    private void tick(ScheduledTask scheduledTask) {
        task = scheduledTask;
        if (!player.isOnline() || !plugin.settings().isEnabled() || !player.isSneaking()) {
            finish(true); // Launch when the player stops sneaking
            return;
        }

        showBar();

        tick++;
        if (tick >= CHARGE_TICKS) {
            finish(true);
        }
    }

    private void retired() {
        cleanup();
        onComplete.run();
    }

    private void showBar() {
        double blocks = currentBlocks();
        String actionbar = plugin.settings().actionbarMessage()
            .replace("{blocks}", String.format("%.1f", blocks));
        player.sendActionBar(TextFormat.legacy(actionbar));

        float progress = getProgress();

        if (plugin.settings().barMode() == BarMode.XP) {
            player.setLevel((int) Math.round(blocks));
            player.setExp(progress);
            return;
        }

        if (bossBar != null) {
            bossBar.setTitle(TextFormat.legacy(String.format("%.1f blocks", blocks)));
            bossBar.setProgress(Math.max(0.0D, Math.min(1.0D, progress)));
        }
    }

    private double currentBlocks() {
        return maxBlocks * getProgress();
    }

    private float getProgress() {
        return (float) tick / CHARGE_TICKS;
    }

    private void finish(boolean launch) {
        if (task != null) {
            task.cancel();
        }
        if (launch && player.isOnline()) {
            launch();
        }
        cleanup();
        onComplete.run();
    }

    private void launch() {
        double blocks = Math.max(0.1D, currentBlocks());
        // This formula is derived from the Minecraft wiki's jump height formula.
        // h = v^2 / (2 * g), where g is gravity (0.08 blocks/tick^2), and v is initial velocity.
        // The player's jump velocity is 0.42.
        // To jump `blocks` high, the required velocity is sqrt(2 * g * blocks).
        // However, we need to factor in the base jump height.
        // A normal jump is 1.25 blocks.
        // The jump boost effect adds to this.
        // A simpler approach is to scale the velocity.
        double y = 0.42 * Math.sqrt(blocks);
        Vector velocity = player.getVelocity();
        velocity.setY(y);
        player.setVelocity(velocity);
    }

    private void cleanup() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (player.isOnline()) {
            player.setLevel(originalLevel);
            player.setExp(originalExp);
        }
    }
}
