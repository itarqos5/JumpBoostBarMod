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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JumpBoostBarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_PATH =
            FMLPaths.CONFIGDIR.get().resolve("jumpboostbar.yml");

    private boolean enabled = true;
    private BarMode barMode = BarMode.XP;
    private String actionbarMessage = "&a{blocks} blocks";

    private JumpBoostBarConfig() {}

    public static JumpBoostBarConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                JumpBoostBarConfig cfg = fromYaml(Files.readAllLines(CONFIG_PATH));
                if (cfg != null) {
                    return cfg;
                }
            } catch (IOException e) {
                System.err.println("[JumpBoostBar] Failed to load config, using defaults: " + e.getMessage());
            }
        }
        JumpBoostBarConfig cfg = new JumpBoostBarConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.write(CONFIG_PATH, toYamlLines());
        } catch (IOException e) {
            System.err.println("[JumpBoostBar] Failed to save config: " + e.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; save(); }

    public BarMode barMode() { return barMode; }
    public void setBarMode(BarMode barMode) { this.barMode = barMode; save(); }

    public String actionbarMessage() { return actionbarMessage; }
    public void setActionbarMessage(String actionbarMessage) { this.actionbarMessage = actionbarMessage; save(); }

    private static JumpBoostBarConfig fromYaml(List<String> lines) {
        JumpBoostBarConfig cfg = new JumpBoostBarConfig();
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }

            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }

            switch (key) {
                case "enabled" -> cfg.enabled = Boolean.parseBoolean(value);
                case "bar" -> cfg.barMode = BarMode.fromConfig(value);
                case "actionbar-message" -> cfg.actionbarMessage = value;
                default -> {
                }
            }
        }
        return cfg;
    }

    private List<String> toYamlLines() {
        List<String> lines = new ArrayList<>();
        lines.add("# JumpBoostBar Configuration");
        lines.add("");
        lines.add("# If false, the jump boost bar will be disabled and not show up when players jump.");
        lines.add("# If true, the jump boost bar will be enabled and show up when players jump.");
        lines.add("enabled: " + enabled);
        lines.add("");
        lines.add("# The type of bar to use for the jump boost bar. Valid options are \"xp\" and \"bossbar\".");
        lines.add("bar: " + barMode.configValue());
        lines.add("");
        lines.add("# The message to display in the action bar when the jump boost bar is active. You can use {blocks} to display the number of blocks the player has jumped.");
        lines.add("actionbar-message: " + GSON.toJson(actionbarMessage));
        return lines;
    }
}
