package com.mineauth.commands;

import com.mineauth.Main;
import com.mineauth.auth.AuthManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogoutCommand implements CommandExecutor {
    private final Main plugin;
    private final AuthManager authManager;

    public LogoutCommand(Main plugin, AuthManager authManager) {
        this.plugin = plugin;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Команда только для игроков.");
            return true;
        }

        if (!authManager.isAuthenticated(p)) {
            p.sendMessage(ChatColor.YELLOW + "Вы не были залогинены.");
            return true;
        }

        authManager.unauth(p);
        p.sendMessage(ChatColor.GREEN + "Вы вышли из аккаунта.");
        return true;
    }
}
