package tech.zmario.enhancedoitc.connector.sql;

import org.bukkit.plugin.Plugin;
import tech.zmario.enhancedoitc.common.objects.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class SQLManager {

    private final ConnectionPoolManager pool;
    private final Plugin plugin;

    private final Executor executor = Executors.newCachedThreadPool();

    public SQLManager(Plugin plugin) {
        this.plugin = plugin;
        pool = new ConnectionPoolManager(plugin);
    }

    public void onDisable() {
        pool.closePool();
    }

    public void createUser(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = pool.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO user_data " +
                         "(uuid, kills, deaths, wins, losses) VALUES (?, ?, ?, ?, ?)")) {
                statement.setString(1, uuid.toString());
                statement.setInt(2, 0);
                statement.setInt(3, 0);
                statement.setInt(4, 0);
                statement.setInt(5, 0);
                statement.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create user with UUID " + uuid, ex);
            }
        }, executor);
    }

    public CompletableFuture<Optional<User>> getUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = pool.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_data WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(new User(uuid, resultSet.getInt("kills"), resultSet.getInt("deaths"),
                                resultSet.getInt("wins"), resultSet.getInt("losses")));
                    }
                }
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get user with UUID " + uuid, ex);
            }

            return Optional.empty();
        }, executor);
    }

    public void updateUser(User user) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = pool.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE user_data SET kills = ?, deaths = ?, wins = ?, losses = ? WHERE uuid = ?")) {
                statement.setInt(1, user.getKills());
                statement.setInt(2, user.getDeaths());
                statement.setInt(3, user.getWins());
                statement.setInt(4, user.getLosses());
                statement.setString(5, user.getUuid().toString());
                statement.executeUpdate();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to update user with UUID " + user.getUuid(), ex);
            }
        }, executor);
    }
}