package tech.zmario.enhancedoitc.connector.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private final Plugin plugin;

    private HikariDataSource dataSource;

    private String hostname, port, database, username, password;

    public ConnectionPoolManager(Plugin plugin) {
        this.plugin = plugin;
        init();
        setupPool();
        makeTables();
    }

    private void init() {
        hostname = plugin.getConfig().getString("mysql.host");
        port = plugin.getConfig().getString("mysql.port");
        username = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");
        database = plugin.getConfig().getString("mysql.database");
    }

    private void setupPool() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useSSL", plugin.getConfig().getBoolean("mysql.ssl"));

        config.setPoolName("EnhancedOITC");

        dataSource = new HikariDataSource(config);
    }

    private void makeTables() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `user_data`" +
                     " (`uuid` VARCHAR(36) NOT NULL, `kills` INT NOT NULL, `deaths` INT NOT NULL, `wins` INT NOT NULL, " +
                     "`losses` INT NOT NULL, PRIMARY KEY (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to create the user data table!");
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}