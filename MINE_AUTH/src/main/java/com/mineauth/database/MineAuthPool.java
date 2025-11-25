package com.mineauth.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Класс для управления пулом соединений MySQL через HikariCP
 */
public class MineAuthPool {

    private HikariDataSource dataSource;

    // Конструктор получает данные для подключения
    public MineAuthPool(String host, String port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&characterEncoding=utf8");
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Настройки пула
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(config);
    }

    // Получаем соединение из пула
    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("DataSource is not initialized");
        return dataSource.getConnection();
    }

    // Запуск пула
    public void start() {
        // ничего не делаем — инициализация уже происходит в конструкторе
    }

    // Остановка пула
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
