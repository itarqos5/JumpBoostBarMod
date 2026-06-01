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

import gg.literal.jumpboostbar.common.JumpBoostBarCommon;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class JumpBoostBarPaper extends JavaPlugin {
    private JumpBoostBarSettings settings;
    private JumpBoostChargeManager chargeManager;

    @Override
    public void onEnable() {
        settings = new JumpBoostBarSettings(this);
        settings.load();

        chargeManager = new JumpBoostChargeManager(this);
        getServer().getPluginManager().registerEvents(chargeManager, this);

        JumpBoostBarCommand command = new JumpBoostBarCommand(this);
        registerPaperCommand(command);

        getLogger().info(JumpBoostBarCommon.startupMessage("Paper"));
    }

    @Override
    public void onDisable() {
        if (chargeManager != null) {
            chargeManager.stopAll();
        }
    }

    public JumpBoostBarSettings settings() {
        return settings;
    }

    public JumpBoostChargeManager chargeManager() {
        return chargeManager;
    }

    private void registerPaperCommand(JumpBoostBarCommand command) {
        try {
            Method registerCommand = JavaPlugin.class.getMethod(
                "registerCommand",
                String.class,
                String.class,
                Collection.class,
                io.papermc.paper.command.brigadier.BasicCommand.class
            );
            registerCommand.invoke(this, "jumpboostbar", "Configure JumpBoostBar.", List.of("jbb"), command);
        } catch (NoSuchMethodException exception) {
            Bukkit.getCommandMap().register("jumpboostbar", new JumpBoostBarBukkitCommand(command));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not access Paper command registration.", exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Paper command registration failed.", exception.getCause());
        }
    }
}
