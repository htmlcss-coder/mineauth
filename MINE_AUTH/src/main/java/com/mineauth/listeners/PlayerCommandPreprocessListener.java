package com.mineauth.listeners;

import com.mineauth.auth.AuthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessListener implements Listener {

    private final AuthManager authManager;

    public PlayerCommandPreprocessListener(AuthManager authManager) {
        this.authManager = authManager;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Если игрок еще не авторизован, надо блокировать ВСЁ
        if (!authManager.isAuthenticated(player.getUniqueId())) {

            // Разрешённые команды
            String message = event.getMessage().toLowerCase();

            if (
                message.startsWith("/login") ||
                message.startsWith("/l ") ||
                message.equals("/l") ||
                message.startsWith("/register") ||
                message.startsWith("/reg ") ||
                message.equals("/reg")
            ) {
                return; // Разрешаем
            }

            // Блокируем все остальные
            event.setCancelled(true);
            player.sendMessage("§cВы должны войти! Используйте §e/login <пароль>");
        }
    }
}
