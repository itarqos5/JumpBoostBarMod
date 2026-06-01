package gg.literal.jumpboostbar.paper;

import gg.literal.jumpboostbar.common.JumpBoostBarCommon;
import org.bukkit.plugin.java.JavaPlugin;

public final class JumpBoostBarPaper extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info(JumpBoostBarCommon.startupMessage("Paper"));
    }
}
