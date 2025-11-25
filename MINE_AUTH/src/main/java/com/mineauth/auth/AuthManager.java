package com.mineauth.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Менеджер авторизации.
 * Хранит авторизованных игроков по UUID.
 * Предоставляет удобные перегрузки для Player и UUID.
 */
public class AuthManager {

    private final JavaPlugin plugin;

    private final Set<UUID> authenticatedPlayers = new HashSet<>();
    private final Map<UUID, Integer> loginAttempts = new HashMap<>();
    private final Map<UUID, Long> blockedUntil = new HashMap<>();

    private final int maxAttempts;
    private final long blockMillis;
    private final int kickDelaySeconds;

    public AuthManager(JavaPlugin plugin, int maxAttempts, long blockMillis, int kickDelaySeconds) {
        this.plugin = plugin;
        this.maxAttempts = maxAttempts;
        this.blockMillis = blockMillis;
        this.kickDelaySeconds = kickDelaySeconds;
    }

    // --------------------
    // Проверки / статус
    // --------------------

    public boolean isAuthenticated(UUID uuid) {
        return authenticatedPlayers.contains(uuid);
    }

    public boolean isAuthenticated(Player p) {
        return isAuthenticated(p.getUniqueId());
    }

    // --------------------
    // Аутентификация / деаутентификация
    // --------------------

    public void authenticate(UUID uuid) {
        authenticatedPlayers.add(uuid);
        loginAttempts.remove(uuid);
        blockedUntil.remove(uuid);
    }

    public void authenticate(Player p) {
        authenticate(p.getUniqueId());
    }

    public void deauthenticate(UUID uuid) {
        authenticatedPlayers.remove(uuid);
    }

    // синоним — чтобы нигде не ломались вызовы
    public void deauthenticate(Player p) {
        deauthenticate(p.getUniqueId());
    }

    // короткая версия (если где-то использовалась autre название)
    public void unauth(Player p) {
        deauthenticate(p);
    }

    // --------------------
    // Попытки входа / блокировка
    // --------------------

    public boolean isBlocked(UUID uuid) {
        Long until = blockedUntil.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            blockedUntil.remove(uuid);
            return false;
        }
        return true;
    }

    public boolean isBlocked(Player p) {
        return isBlocked(p.getUniqueId());
    }

    public long getBlockedMillisLeft(UUID uuid) {
        return Math.max(0L, blockedUntil.getOrDefault(uuid, 0L) - System.currentTimeMillis());
    }

    public long getBlockedMillisLeft(Player p) {
        return getBlockedMillisLeft(p.getUniqueId());
    }

    /**
     * Регистрация неудачной попытки. Возвращает true, если ещё можно пытаться,
     * false — если достигнут лимит и игрок заблокирован.
     */
    public boolean registerAttempt(UUID uuid) {
        int attempts = loginAttempts.getOrDefault(uuid, 0) + 1;
        loginAttempts.put(uuid, attempts);

        if (attempts >= maxAttempts) {
            blockedUntil.put(uuid, System.currentTimeMillis() + blockMillis);
            loginAttempts.remove(uuid);
            return false;
        }
        return true;
    }

    public boolean registerAttempt(Player p) {
        return registerAttempt(p.getUniqueId());
    }

    // --------------------
    // Плановый кик
    // --------------------

    /**
     * Планируем кик игрока, если он не авторизовался к концу delay.
     * Использует scheduler сервера (20 ticks = 1 sec).
     */
    public void scheduleKickIfNotAuth(UUID uuid) {
        // нужно получить объект Player — может быть null, поэтому проверяем
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) return;
            if (!isAuthenticated(uuid)) {
                p.kickPlayer("§cВы не авторизовались вовремя. Используйте /login <пароль> или /register <пароль>.");
            }
        }, kickDelaySeconds * 20L);
    }

    public void scheduleKickIfNotAuth(Player p) {
        scheduleKickIfNotAuth(p.getUniqueId());
    }
}
