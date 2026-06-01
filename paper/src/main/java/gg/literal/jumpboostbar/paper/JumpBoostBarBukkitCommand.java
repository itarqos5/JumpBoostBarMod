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
