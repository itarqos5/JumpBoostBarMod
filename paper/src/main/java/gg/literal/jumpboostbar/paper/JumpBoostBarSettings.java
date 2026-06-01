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

import org.bukkit.configuration.file.FileConfiguration;

public final class JumpBoostBarSettings {
    private final JumpBoostBarPaper plugin;
    private boolean enabled;
    private BarMode barMode;
    private String actionbarMessage;

    public JumpBoostBarSettings(JumpBoostBarPaper plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("enabled", false);
        barMode = BarMode.fromConfig(config.getString("bar", "xp"));
        actionbarMessage = config.getString("actionbar-message", "&a{blocks} blocks");
    }

    public void save() {
        FileConfiguration config = plugin.getConfig();
        config.set("enabled", enabled);
        config.set("bar", barMode.configValue());
        config.set("actionbar-message", actionbarMessage);
        plugin.saveConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public BarMode barMode() {
        return barMode;
    }

    public void setBarMode(BarMode barMode) {
        this.barMode = barMode;
        save();
    }

    public String actionbarMessage() {
        return actionbarMessage;
    }
}
