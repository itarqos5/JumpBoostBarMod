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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JumpBoostBarConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("jumpboostbar.json");

    private boolean enabled = true;
    private String actionbarMessage = "&a{blocks} blocks";

    private JumpBoostBarConfig() {}

    public static JumpBoostBarConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JumpBoostBarConfig cfg = GSON.fromJson(reader, JumpBoostBarConfig.class);
                if (cfg != null) return cfg;
            } catch (IOException e) {
                System.err.println("[JumpBoostBar] Failed to load config, using defaults: " + e.getMessage());
            }
        }
        JumpBoostBarConfig cfg = new JumpBoostBarConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("[JumpBoostBar] Failed to save config: " + e.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; save(); }

    public String actionbarMessage() { return actionbarMessage; }
    public void setActionbarMessage(String actionbarMessage) { this.actionbarMessage = actionbarMessage; save(); }
}
