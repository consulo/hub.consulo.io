package consulo.hub.shared.repository;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
public class FrontPluginVersionInfo {
    public String myVersion;

    public Set<PluginChannel> myChannels = new TreeSet<>();

    public Long myDate;

    public FrontPluginVersionInfo() {

    }

    public FrontPluginVersionInfo(String version) {
        myVersion = version;
    }
}
