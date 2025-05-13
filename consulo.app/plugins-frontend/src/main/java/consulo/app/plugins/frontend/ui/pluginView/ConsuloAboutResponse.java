package consulo.app.plugins.frontend.ui.pluginView;

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
    }

    public boolean success;
    public Data data;
}
