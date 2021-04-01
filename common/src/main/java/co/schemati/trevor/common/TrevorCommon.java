package co.schemati.trevor.common;

import co.schemati.trevor.api.TrevorAPI;
import co.schemati.trevor.api.TrevorService;
import co.schemati.trevor.api.TrevorState;
import co.schemati.trevor.api.data.Platform;
import co.schemati.trevor.api.database.Database;
import co.schemati.trevor.api.database.DatabaseConnection;
import co.schemati.trevor.api.instance.InstanceData;
import co.schemati.trevor.common.proxy.DatabaseProxyImpl;
import com.google.gson.Gson;

public class TrevorCommon implements TrevorAPI {

  private static Gson gson;

  private final Platform platform;

  private Database database;
  private DatabaseProxyImpl proxy;

  private InstanceData data;

  private TrevorState state = TrevorState.NOT_LOADED;

  public TrevorCommon(Platform platform) {
    this.platform = platform;
  }

  public boolean initPlatform() {
    boolean success = platform.init();

    if (!success) {
      state = TrevorState.FAILED;
      return false;
    }

    return true;
  }

  public boolean load() {
    TrevorService.setAPI(this);

    // TODO: Verify instance configuration values before pool creation
    gson = new Gson();

    this.data = new InstanceData();

    this.database = platform.getDatabaseConfiguration().create(platform, data, gson);

    this.proxy = new DatabaseProxyImpl(platform, database);

    // TODO Should we do a database health check here before true / false?

    return true;
  }

  public boolean start() {
    boolean inited = database.init(proxy);

    if (!inited) {
      state = TrevorState.FAILED;
      return false;
    }

    state = TrevorState.RUNNING;
    return true;
  }

  public boolean stop() {
    state = TrevorState.STOPPED;

    if (database != null) {
      database.open().thenAccept(DatabaseConnection::shutdown).join();

      database.kill();
    }

    return true;
  }

  public InstanceData getInstanceData() {
    return data;
  }

  public Platform getPlatform() {
    return platform;
  }

  public Database getDatabase() {
    return database;
  }

  public DatabaseProxyImpl getDatabaseProxy() {
    return proxy;
  }

  public TrevorState getState() {
    return state;
  }

  public static Gson gson() {
    return gson;
  }
}
