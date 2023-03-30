package tech.zmario.enhancedoitc.common.handler;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import tech.zmario.enhancedoitc.common.enums.SettingsConfiguration;
import tech.zmario.enhancedoitc.common.redis.codec.SerializedObjectCodec;
import tech.zmario.enhancedoitc.common.redis.listener.PubSubListener;

@RequiredArgsConstructor
@Getter
public class RedisHandler {

    private final Plugin plugin;
    private final StatefulRedisConnection<String, String> connection;

    private final RedisPubSubAsyncCommands<String, Object> pubSubCommands;

    public RedisHandler(Plugin plugin, String redisUri) {
        this.plugin = plugin;

        try (RedisClient client = RedisClient.create(RedisURI.create(redisUri))) {
            connection = client.connect();
            pubSubCommands = client.connectPubSub(new SerializedObjectCodec()).async();
        }
    }

    public void publish(String channel, Object message) {
        pubSubCommands.publish(channel, message);
    }

    public void subscribe(String channel, PubSubListener<String, Object> listener) {
        pubSubCommands.getStatefulConnection().addListener(listener);
        pubSubCommands.subscribe(channel);
    }
}
