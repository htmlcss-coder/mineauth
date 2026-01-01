package com.mineauth;

import com.mineauth.auth.AuthManager;
import com.mineauth.commands.LoginCommand;
import com.mineauth.commands.LogoutCommand;
import com.mineauth.commands.RegisterCommand;
import com.mineauth.database.MineAuthPool;
import com.mineauth.listeners.PlayerCommandPreprocessListener;
import com.mineauth.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private MineAuthPool mineAuthPool;
    private AuthManager authManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String host = getConfig().getString("mysql.host", "Your_MySQL_Host_Here");
        String port = getConfig().getString("mysql.port", "3306");
        String database = getConfig().getString("mysql.database", "Your_Database_Here");
        String username = getConfig().getString("mysql.username", "Your_MySQL_User_Here");
        String password = getConfig().getString("mysql.password", "Your_MySQL_Password_Here");

        int maxAttempts = getConfig().getInt("security.maxAttempts", 5);
        long blockMillis = getConfig().getLong("security.blockMillis", 2 * 60 * 1000); // 2 min default
        int kickDelaySeconds = getConfig().getInt("security.kickDelaySeconds", 60);

        try {
            mineAuthPool = new MineAuthPool(host, port, database, username, password);
        } catch (Exception e) {
            getLogger().severe("[MineAuth] Не удалось инициализировать пул: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        authManager = new AuthManager(this, maxAttempts, blockMillis, kickDelaySeconds);

        PlayerJoinListener joinListener = new PlayerJoinListener(mineAuthPool, authManager);
        getServer().getPluginManager().registerEvents(joinListener, this);
        getServer().getPluginManager().registerEvents(
        new PlayerCommandPreprocessListener(authManager),
        this
);


        if (getCommand("register") != null)
            getCommand("register").setExecutor(new RegisterCommand(this, mineAuthPool, authManager));
        if (getCommand("login") != null)
            getCommand("login").setExecutor(new LoginCommand(this, mineAuthPool, authManager));
        if (getCommand("logout") != null)
            getCommand("logout").setExecutor(new LogoutCommand(this, authManager));

        getLogger().info("[MineAuth] Плагин успешно включён!");
    }

    @Override
    public void onDisable() {
        if (mineAuthPool != null) mineAuthPool.close();
        getLogger().info("[MineAuth] MineAuth выключен.");
    }
}
