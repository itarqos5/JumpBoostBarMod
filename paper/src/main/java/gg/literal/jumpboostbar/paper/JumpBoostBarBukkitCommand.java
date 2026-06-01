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

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public final class JumpBoostBarBukkitCommand extends Command {
    private final JumpBoostBarCommand delegate;

    public JumpBoostBarBukkitCommand(JumpBoostBarCommand delegate) {
        super("jumpboostbar", "Configure JumpBoostBar.", "/jumpboostbar <on|off|bar>", List.of("jbb"));
        this.delegate = delegate;
        setPermission(delegate.permission());
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        delegate.execute(sender, args);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return delegate.suggest(args);
    }
}
