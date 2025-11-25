package com.mineauth.dao;

import org.bukkit.configuration.file.FileConfiguration;
import java.sql.*;

public class Database {
    private Connection connection;

    public void connect(FileConfiguration config) {
        String host = config.getString("mysql.host", "localhost");
        String port = config.getString("mysql.port", "3306");
        String database = config.getString("mysql.database", "MineAuth_DB");
        String user = config.getString("mysql.user", "MineAuth_SQL");
        String password = config.getString("mysql.password", "MineAuth_8396");

        try {
            if (connection != null && !connection.isClosed()) return;
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    user,
                    password
            );
            System.out.println("[MineAuth] ✅ Successfully connected to MySQL!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[MineAuth] ❌ MySQL connection failed: " + e.getMessage());
        }
    }

    public boolean isPlayerRegistered(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE minecraft_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void registerPlayer(String username, String password) throws SQLException {
        String query = "INSERT INTO users (minecraft_name, password_hash) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // TODO: заменить на hash (SHA-256)
            stmt.executeUpdate();
        }
    }

    public boolean validatePassword(String username, String password) throws SQLException {
        String query = "SELECT password_hash FROM users WHERE minecraft_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password_hash");
                    return stored.equals(password); // TODO: заменить на hash comparison
                }
            }
        }
        return false;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
