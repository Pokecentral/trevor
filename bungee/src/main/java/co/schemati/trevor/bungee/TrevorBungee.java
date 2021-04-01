package co.schemati.trevor.bungee;

import co.schemati.trevor.api.TrevorState;
import co.schemati.trevor.bungee.platform.BungeeListener;
import co.schemati.trevor.bungee.platform.BungeePlatform;
import co.schemati.trevor.common.TrevorCommon;
import net.md_5.bungee.api.plugin.Plugin;

public class TrevorBungee extends Plugin {

  private BungeePlatform platform;
  private TrevorCommon common;

  @Override
  public void onLoad() {
    this.platform = new BungeePlatform(this);
    this.common = new TrevorCommon(platform);

    if (!common.initPlatform()) {
      platform.log("Trevor failed to load platform... Shutting down.");
      return;
    }

    if (!common.load()) {
      platform.log("Trevor failed to load... Shutting down.");
      return;
    }
  }

  @Override
  public void onEnable() {
    if (common.getState() == TrevorState.FAILED) {
      platform.log("Trevor state is FAILED - Skipping onEnable logic.");
      return;
    }

    getProxy().getPluginManager().registerListener(this, new BungeeListener(this));

    if (!common.start()) {
      platform.log("Trevor failed to start... Shutting down.");
      return;
    }
  }

  @Override
  public void onDisable() {
    common.stop();
  }

  public BungeePlatform getPlatform() {
    return platform;
  }

  public TrevorCommon getCommon() {
    return common;
  }
}
