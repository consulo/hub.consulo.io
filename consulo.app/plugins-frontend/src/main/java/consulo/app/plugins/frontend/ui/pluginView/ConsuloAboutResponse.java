package consulo.app.plugins.frontend.ui.pluginView;

import java.util.List;

/**
 * @author VISTALL
 * @since 2025-05-13
 *
 * {
"success": true,
"data": {
"name": "Consulo",
"build": 3862,
"channel": "nightly"
}
}
 */
public class ConsuloAboutResponse {
    public static class Data {
        public String name;
        public int build;
        public String channel;
        public List<String> plugins = List.of();
    }

    public boolean success;
    public Data data;
}
