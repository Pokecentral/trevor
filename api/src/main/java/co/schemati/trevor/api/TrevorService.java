package co.schemati.trevor.api;

import java.util.Optional;

/**
 * Represents a place to retrieve the provided {@link TrevorAPI}.
 */
public class TrevorService {

  private static TrevorAPI api;
  private static final Object lock = new Object();

  /**
   * Returns the provided {@link TrevorAPI}.
   *
   * @return the trevor api
   */
  public static TrevorAPI getAPI() {
    return api;
  }

  /**
   * Provides an implementation of {@link TrevorAPI}.
   *
   * <br>
   * <p>
   * If an implementation is already defined, a {@link IllegalStateException} is thrown.
   *
   * @param api the api
   */
  public static void setAPI(TrevorAPI api) {
    if (TrevorService.api == null) {
      synchronized (lock) {
        if (TrevorService.api == null) {
          TrevorService.api = api;
          return;
        }
      }
    }
    throw new IllegalStateException("Singleton TrevorAPI cannot be redefined.");
  }

  /**
   * Returns the state of the active api. If this is called before the Trevor implementation provided the api to the
   * service, an empty optional is returned.
   *
   * @return The state of the api
   */
  public static Optional<TrevorState> getState() {
    TrevorAPI api = TrevorService.api;

    if (api == null) {
      return Optional.empty();
    }

    return Optional.of(api.getState());
  }
}
