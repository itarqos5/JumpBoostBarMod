package gg.literal.jumpboostbar.paper;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Random;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class JumpChargeSession {
    private static final int CHARGE_TICKS = 45;

    private final JumpBoostBarPaper plugin;
    private final Player player;
    private final double maxBlocks;
    private final Runnable onComplete;
    private final Random random = new Random();

    private ScheduledTask task;
    private BossBar bossBar;
    private int tick;
    private float progress;
    private float velocity;
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
        if (!player.isOnline() || !plugin.settings().isEnabled()) {
            finish(false);
            return;
        }

        updateHorseLikeProgress();
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

    private void updateHorseLikeProgress() {
        if (tick == 0 || random.nextDouble() < 0.22D) {
            velocity = (random.nextFloat() * 0.16F) - 0.07F;
            if (progress < 0.18F) {
                velocity = Math.abs(velocity) + 0.04F;
            } else if (progress > 0.92F) {
                velocity = -Math.abs(velocity) - 0.03F;
            }
        }

        progress = clamp(progress + velocity, 0.0F, 1.0F);
    }

    private void showBar() {
        double blocks = currentBlocks();
        String actionbar = plugin.settings().actionbarMessage()
            .replace("{blocks}", String.format("%.1f", blocks));
        player.sendActionBar(TextFormat.legacy(actionbar));

        if (plugin.settings().barMode() == BarMode.XP) {
            player.setLevel((int) Math.round(blocks * 10.0D) / 10);
            player.setExp(progress);
            return;
        }

        if (bossBar != null) {
            bossBar.setTitle(TextFormat.legacy(String.format("%.1f blocks", blocks)));
            bossBar.setProgress(Math.max(0.0D, Math.min(1.0D, progress)));
        }
    }

    private double currentBlocks() {
        return Math.round(maxBlocks * progress * 10.0D) / 10.0D;
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
        double y = 0.42D * Math.sqrt(blocks / 1.25D);
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

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
