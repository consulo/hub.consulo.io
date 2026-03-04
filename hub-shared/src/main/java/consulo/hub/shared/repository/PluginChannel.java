package consulo.hub.shared.repository;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public enum PluginChannel {
    release, // every month
    beta,    // every week
    alpha,   // every day
    nightly, // every commit
    // used for 3-SNAPSHOT - module system, refactoring
    // used for 4-SNAPSHOT - rewrite lock system
    valhalla
}
