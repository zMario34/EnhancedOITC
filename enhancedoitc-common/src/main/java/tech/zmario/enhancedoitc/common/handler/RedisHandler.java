package tech.zmario.enhancedoitc.common.handler;

import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import tech.zmario.enhancedoitc.common.objects.User;
import tech.zmario.enhancedoitc.common.redis.codec.SerializedObjectCodec;
import tech.zmario.enhancedoitc.common.redis.listener.PubSubListener;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@RequiredArgsConstructor
@Getter
public class RedisHandler {

    private final Plugin plugin;
    private final RedisAsyncCommands<String, String> commands;

    private final RedisPubSubAsyncCommands<String, Object> pubCommands;
    private final RedisPubSubAsyncCommands<String, Object> subCommands;

    private final Gson gson = new Gson();

    public RedisHandler(Plugin plugin, String redisUri) {
        this.plugin = plugin;

        RedisClient client = RedisClient.create(RedisURI.create(redisUri));

        commands = client.connect().async();
        pubCommands = client.connectPubSub(new SerializedObjectCodec()).async();
        subCommands = client.connectPubSub(new SerializedObjectCodec()).async();
    }

    public void publish(String channel, Object message) {
        pubCommands.publish(channel, message);
    }

    public void subscribe(String channel, PubSubListener<String, Object> listener) {
        subCommands.getStatefulConnection().addListener(listener);
        subCommands.subscribe(channel);
    }

    public void updateUser(User user) {
        commands.hset("users", user.getUuid().toString(), gson.toJson(user));
    }

    public CompletionStage<User> getUser(UUID uuid) {
        return commands.hget("users", uuid.toString()).thenApply(s -> gson.fromJson(s, User.class));
    }
}
