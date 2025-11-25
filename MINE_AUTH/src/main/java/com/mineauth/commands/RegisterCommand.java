package com.mineauth.commands;

import com.mineauth.Main;
import com.mineauth.auth.AuthManager;
import com.mineauth.database.MineAuthPool;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterCommand implements CommandExecutor {
    private final Main plugin;
    private final MineAuthPool pool;
    private final AuthManager authManager;

    public RegisterCommand(Main plugin, MineAuthPool pool, AuthManager authManager) {
        this.plugin = plugin;
        this.pool = pool;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Команда только для игроков.");
            return true;
        }

        if (authManager.isBlocked(p)) {
            long left = authManager.getBlockedMillisLeft(p) / 1000;
            p.sendMessage(ChatColor.RED + "Вы временно заблокированы от попыток входа. Подождите " + left + " сек.");
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.YELLOW + "Использование: /register <пароль>");
            return true;
        }

        String pass = args[0];

        try (Connection c = pool.getConnection();
             PreparedStatement check = c.prepareStatement("SELECT id FROM users WHERE minecraft_name = ?")) {
            check.setString(1, p.getName());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    p.sendMessage(ChatColor.RED + "Ник уже зарегистрирован.");
                    return true;
                }
            }

            String hashed = BCrypt.hashpw(pass, BCrypt.gensalt());
            try (PreparedStatement insert = c.prepareStatement("INSERT INTO users (minecraft_name, password_hash) VALUES (?, ?)")) {
                insert.setString(1, p.getName());
                insert.setString(2, hashed);
                insert.executeUpdate();
            }

            authManager.authenticate(p);
            p.sendMessage(ChatColor.GREEN + "Вы успешно зарегистрированы и авторизованы!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("[MineAuth] Ошибка при регистрации: " + e.getMessage());
            p.sendMessage(ChatColor.RED + "Ошибка сервера при регистрации. Смотри логи.");
            return true;
        }
    }
}
