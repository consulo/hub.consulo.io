package consulo.hub.shared.repository;

import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
public class FrontPluginNodeById {
    public PluginNode myPluginNode;

    public int myDownloads;

    public TreeMap<String, FrontPluginVersionInfo> myVersions = new TreeMap<>();

    public FrontPluginNodeById() {
    }

    public FrontPluginNodeById(PluginNode node) {
        myPluginNode = node.clone();
    }
}
