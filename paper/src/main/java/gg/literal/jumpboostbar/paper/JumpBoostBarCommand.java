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

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;

public final class JumpBoostBarCommand implements BasicCommand {
    private static final List<String> ROOT = List.of("on", "off", "bar");
    private static final List<String> BAR_MODES = List.of("xp", "bossbar");

    private final JumpBoostBarPaper plugin;

    public JumpBoostBarCommand(JumpBoostBarPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        execute(source.getSender(), args);
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("on")) {
            plugin.settings().setEnabled(true);
            sender.sendMessage(TextFormat.legacy("&aJumpBoostBar enabled."));
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("off")) {
            plugin.settings().setEnabled(false);
            plugin.chargeManager().stopAll();
            sender.sendMessage(TextFormat.legacy("&cJumpBoostBar disabled."));
            return;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("bar")) {
            BarMode mode = BarMode.fromConfig(args[1]);
            if (!args[1].equalsIgnoreCase("xp") && !args[1].equalsIgnoreCase("bossbar")) {
                sender.sendMessage(TextFormat.legacy("&cUsage: /jumpboostbar bar <xp|bossbar>"));
                return;
            }

            plugin.settings().setBarMode(mode);
            plugin.chargeManager().stopAll();
            sender.sendMessage(TextFormat.legacy("&aJumpBoostBar bar set to &f" + mode.configValue() + "&a."));
            return;
        }

        sender.sendMessage(TextFormat.legacy("&e/jumpboostbar on"));
        sender.sendMessage(TextFormat.legacy("&e/jumpboostbar off"));
        sender.sendMessage(TextFormat.legacy("&e/jumpboostbar bar <xp|bossbar>"));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return suggest(args);
    }

    public List<String> suggest(String[] args) {
        if (args.length == 1) {
            return matching(ROOT, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("bar")) {
            return matching(BAR_MODES, args[1]);
        }

        return List.of();
    }

    @Override
    public String permission() {
        return "jumpboostbar.command";
    }

    private static List<String> matching(List<String> values, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String value : values) {
            if (value.startsWith(normalized)) {
                matches.add(value);
            }
        }
        return matches;
    }
}
