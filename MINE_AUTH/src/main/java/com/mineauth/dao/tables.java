package com.mineauth.dao;

import java.sql.Connection;
import java.sql.Statement;

public class tables {

    public static void createUsersTable(Database db) {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "minecraft_name VARCHAR(16) NOT NULL," +
                    "password_hash VARCHAR(255) NOT NULL," +
                    "email VARCHAR(255) DEFAULT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";

            stmt.executeUpdate(sql);
            System.out.println("[MineAuth] ✅ Table 'users' verified or created successfully!");

        } catch (Exception e) {
            System.out.println("[MineAuth] ❌ Failed to create users table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
