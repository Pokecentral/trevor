package co.schemati.trevor.common.database.redis;

import co.schemati.trevor.api.data.Platform;
import co.schemati.trevor.api.database.DatabaseConfiguration;
import co.schemati.trevor.api.instance.InstanceData;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

public class RedisConfiguration implements DatabaseConfiguration {

  private final String address;
  private final short port;
  private final String password;
  private final int maxConnections;
  private final boolean useSSL;
  private final int timeout;

  public RedisConfiguration(String address, short port, String password, int maxConnections,
                            boolean useSSL,
                            int timeout) {
    this.address = address;
    this.port = port;
    this.password = password != null && password.isEmpty() ? null : password;
    this.maxConnections = maxConnections;
    this.useSSL = useSSL;
    this.timeout = timeout;
  }

  @Override
  public RedisDatabase create(Platform platform, InstanceData data, Gson gson) {
    JedisPoolConfig config = new JedisPoolConfig();

    config.setMaxTotal(maxConnections);

    JedisPool pool = new JedisPool(config, address, port, timeout, password, useSSL);

    try (Jedis jedis = pool.getResource()) {
      jedis.ping();
    } catch (JedisException e) {
      e.printStackTrace();
      platform.log("Failed to ping redis database. Are your database credentials OK?");
      pool.close();
      return null;
    }

    return new RedisDatabase(platform, data, pool, gson);
  }
}
