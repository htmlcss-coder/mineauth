package com.mineauth.listeners;

import com.mineauth.auth.AuthManager;
import com.mineauth.database.MineAuthPool;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityDamageEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerJoinListener implements Listener {
    private final MineAuthPool pool;
    private final AuthManager authManager;

    public PlayerJoinListener(MineAuthPool pool, AuthManager authManager) {
        this.pool = pool;
        this.authManager = authManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        boolean registered = false;
        try (Connection c = pool.getConnection();
             PreparedStatement st = c.prepareStatement("SELECT id FROM users WHERE minecraft_name = ?")) {
            st.setString(1, p.getName());
            try (ResultSet rs = st.executeQuery()) {
                registered = rs.next();
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[MineAuth] Ошибка при проверке пользователя на join: " + e.getMessage());
            p.sendMessage(ChatColor.RED + "Ошибка проверки аккаунта. Смотри логи сервера.");
        }

        if (!registered) {
            p.sendMessage(ChatColor.YELLOW + "Зарегистрируйтесь: /register <пароль>");
        } else {
            p.sendMessage(ChatColor.GREEN + "Введите /login <пароль> для входа.");
        }

        // запланируем кик если не авторизуется
        authManager.scheduleKickIfNotAuth(p);
    }

    // блокируем чат если не аутентифицирован
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (!authManager.isAuthenticated(p)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.YELLOW + "Вы не аутентифицированы. Введите /login или /register.");
        }
    }

    // блокируем передвижение/интеракции если не аутентифицирован (простая версия)
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (!authManager.isAuthenticated(p)) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!authManager.isAuthenticated(p)) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.YELLOW + "Авторизуйтесь, чтобы взаимодействовать.");
        }
    }

    // optional: предотвращать урон до авторизации
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player p) {
            if (!authManager.isAuthenticated(p)) {
                event.setCancelled(true);
            }
        }
    }
}
