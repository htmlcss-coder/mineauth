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

public class LoginCommand implements CommandExecutor {
    private final Main plugin;
    private final MineAuthPool pool;
    private final AuthManager authManager;

    public LoginCommand(Main plugin, MineAuthPool pool, AuthManager authManager) {
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
            p.sendMessage(ChatColor.RED + "Вы заблокированы от попыток входа. Подождите " + left + " сек.");
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.YELLOW + "Использование: /login <пароль>");
            return true;
        }

        String pass = args[0];

        try (Connection c = pool.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT password_hash FROM users WHERE minecraft_name = ?")) {
            st.setString(1, p.getName());
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    p.sendMessage(ChatColor.RED + "Ник не найден. Зарегистрируйтесь: /register <пароль>");
                    return true;
                }
                String hash = rs.getString("password_hash");
                if (hash == null || !BCrypt.checkpw(pass, hash)) {
                    // register attempt and maybe block
                    boolean canContinue = authManager.registerAttempt(p);
                    if (!canContinue) {
                        long left = authManager.getBlockedMillisLeft(p) / 1000;
                        p.sendMessage(ChatColor.RED + "Слишком много попыток. Блок на " + left + " сек.");
                    } else {
                        p.sendMessage(ChatColor.RED + "Неверный пароль.");
                    }
                    return true;
                }
            }

            authManager.authenticate(p);
            p.sendMessage(ChatColor.GREEN + "Успешный вход!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("[MineAuth] Ошибка при логине: " + e.getMessage());
            p.sendMessage(ChatColor.RED + "Ошибка сервера при входе. Смотри логи.");
            return true;
        }
    }
}
