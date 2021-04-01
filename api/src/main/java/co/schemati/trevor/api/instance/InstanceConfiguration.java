package co.schemati.trevor.api.instance;

/**
 * Represents the instance configuration values.
 */
public class InstanceConfiguration {

  private final String id;
  private final boolean shutdownOnFailure;

  /**
   * Construct a new InstanceConfiguration.
   *
   * @param id the instance id
   * @param shutdownOnFailure if the proxy should shut down when initialization fails
   */
  public InstanceConfiguration(String id, boolean shutdownOnFailure) {
    this.id = id;
    this.shutdownOnFailure = shutdownOnFailure;
  }

  /**
   * Returns the instance's id.
   *
   * <br>
   *
   * The id is used to register the instance in the remote database, so it must be unique.
   *
   * @return the id
   */
  public String getID() {
    return id;
  }

  /**
   * @return whether if the proxy should shut down when trevor fails to initialize
   */
  public boolean shouldShutdownOnFailure() {
    return shutdownOnFailure;
  }
}
