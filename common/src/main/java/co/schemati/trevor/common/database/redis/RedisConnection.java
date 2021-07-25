package co.schemati.trevor.common.database.redis;

import co.schemati.trevor.api.data.User;
import co.schemati.trevor.api.database.DatabaseConnection;
import co.schemati.trevor.api.instance.InstanceData;
import co.schemati.trevor.api.network.payload.DisconnectPayload;
import com.google.common.collect.ImmutableList;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static co.schemati.trevor.api.util.Strings.replace;
import static co.schemati.trevor.common.database.redis.RedisDatabase.HEARTBEAT;
import static co.schemati.trevor.common.database.redis.RedisDatabase.INSTANCE_PLAYERS;
import static co.schemati.trevor.common.database.redis.RedisDatabase.PLAYER_DATA;
import static co.schemati.trevor.common.database.redis.RedisDatabase.SERVER_PLAYERS;

public class RedisConnection implements DatabaseConnection {

  private final String instance;
  private final Jedis connection;
  private final InstanceData data;

  public RedisConnection(String instance, Jedis connection, InstanceData data) {
    this.instance = instance;
    this.connection = connection;
    this.data = data;
  }

  @Override
  public void beat() {
    long timestamp = System.currentTimeMillis();

    connection.hset(HEARTBEAT, instance, String.valueOf(timestamp));

    ImmutableList.Builder<String> builder = ImmutableList.builder();
    int playerCount = 0;

    Map<String, String> heartbeats = connection.hgetAll(HEARTBEAT);
    for (Map.Entry<String, String> entry : heartbeats.entrySet()) {
      long lastBeat = Long.parseLong(entry.getValue());
      if (timestamp <= lastBeat + (30 * 1000)) { // 30 seconds
        builder.add(entry.getKey());

        playerCount += connection.scard(replace(INSTANCE_PLAYERS, entry.getKey()));
      } else {
        // TODO: Potentially notify that the instance could be dead.
      }
    }

    data.update(builder.build(), playerCount);
  }

  @Override
  @Deprecated
  public void update(InstanceData data) {
    // Deprecated, moved to beat.
  }

  @Override
  public boolean create(User user) {
    if (isOnline(user)) {
      return false;
    }

    connection.hmset(replace(PLAYER_DATA, user), user.toDatabaseMap(instance));
    connection.sadd(replace(INSTANCE_PLAYERS, instance), user.toString());

    return true;
  }

  @Override
  public DisconnectPayload destroy(UUID uuid) {
    long timestamp = System.currentTimeMillis();

    setServer(uuid, null);

    connection.srem(replace(INSTANCE_PLAYERS, instance), uuid.toString());
    connection.hdel(replace(PLAYER_DATA, uuid), "server", "ip", "instance");
    connection.hset(replace(PLAYER_DATA, uuid), "lastOnline", String.valueOf(timestamp));

    return DisconnectPayload.of(instance, uuid, timestamp);
  }

  @Override
  public void setServer(UUID uuid, @Nullable String server) {
    String user = uuid.toString();
    String previous = connection.hget(replace(PLAYER_DATA, user), "server");
    if (previous != null) {
      connection.srem(replace(SERVER_PLAYERS, previous), user);
    }

    if (server != null) {
      connection.sadd(replace(SERVER_PLAYERS, server), user);
      connection.hset(replace(PLAYER_DATA, user), "server", server);
    }
  }

  @Override
  public void setServer(User user, @Nullable String server) {
    setServer(user.uuid(), server);
  }

  @Override
  public boolean isOnline(User user) {
    return connection.hexists(replace(PLAYER_DATA, user), "server");
  }

  @Override
  public boolean isInstanceAlive() {
    long timestamp = System.currentTimeMillis();
    if (connection.hexists(HEARTBEAT, instance)) {
      long lastBeat = Long.parseLong(connection.hget(HEARTBEAT, instance));
      return timestamp >= lastBeat + (20 * 1000); // 20 seconds in terms of milliseconds
    }
    return false;
  }

  @Override
  public Set<String> getServerPlayers(String server) {
    return connection.smembers(replace(SERVER_PLAYERS, server));
  }

  @Override
  public long getServerPlayerCount(String server) {
    return connection.scard(replace(SERVER_PLAYERS, server));
  }

  @Override
  public Set<String> getNetworkPlayers() {
    return data.getInstances().stream()
            .map(name -> connection.smembers(replace(INSTANCE_PLAYERS, name)))
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
  }

  @Deprecated
  @Override
  public long getNetworkPlayerCount() {
    return data.getPlayerCount();
  }

  @Override
  public void publish(String channel, String message) {
    connection.publish(channel, message);
  }

  @Deprecated
  @Override
  public void deleteHeartbeat() {
    shutdown();
  }

  @Override
  public void shutdown() {
    connection.hdel(HEARTBEAT, instance);
  }

  @Override
  public void close() {
    connection.close();
  }

  @Nullable
  @Override
  public Map<String, String> getUserInformation(UUID uuid) {
    return connection.hgetAll(replace(PLAYER_DATA, uuid.toString()));
  }
}
