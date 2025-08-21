package consulo.hub.pluginAnalyzer;

import consulo.application.ApplicationManager;
import consulo.application.internal.plugin.CompositeMessage;
import consulo.application.internal.plugin.PluginsInitializeInfo;
import consulo.application.internal.plugin.PluginsLoader;
import consulo.component.extension.ExtensionPoint;
import consulo.component.extension.preview.ExtensionPreview;
import consulo.component.extension.preview.ExtensionPreviewRecorder;
import consulo.component.internal.ComponentBinding;
import consulo.component.internal.inject.InjectingBindingLoader;
import consulo.component.internal.inject.TopicBindingLoader;
import consulo.disposer.AutoDisposable;
import consulo.hub.pluginAnalyzer.logger.SilentLoggerFactory;
import consulo.logging.internal.LoggerFactoryInitializer;

import java.util.*;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class Analyzer {
    // called by reflection inside PluginAnalyzerService
    public static List<Map<String, String>> runAnalyzer(String targetPluginId) {
        try (AutoDisposable disposable = AutoDisposable.newAutoDisposable()) {
            LoggerFactoryInitializer.setFactory(new SilentLoggerFactory());

            initOtherPlugins();

            List<ExtensionPreview> previews = new ArrayList<>();

            try (InjectingBindingLoader injectingBindingLoader = new InjectingBindingLoader()) {
                injectingBindingLoader.analyzeBindings();

                AnalyzerApplication application = new AnalyzerApplication(disposable, new ComponentBinding(injectingBindingLoader, new TopicBindingLoader()));
                ApplicationManager.setApplication(application, disposable);

                ExtensionPoint<ExtensionPreviewRecorder> recorders = application.getExtensionPoint(ExtensionPreviewRecorder.class);

                recorders.forEachExtensionSafe(recorder -> recorder.analyze(it ->
                {
                    ExtensionPreview extensionPreview = (ExtensionPreview) it;

                    if (!targetPluginId.equals(extensionPreview.implPluginId().getIdString())) {
                        return;
                    }

                    previews.add(extensionPreview);
                }));
            }

            if (previews.isEmpty()) {
                return List.of();
            }

            List<Map<String, String>> result = new LinkedList<>();
            for (ExtensionPreview preview : previews) {
                Map<String, String> map = new HashMap<>();
                map.put("apiClassName", preview.apiClassName());
                map.put("apiPluginId", preview.apiPluginId().toString());
                map.put("implId", preview.implId());
                map.put("implPluginId", preview.implPluginId().toString());
                result.add(map);
            }
            return result;
        }
    }

    private static void initOtherPlugins() {
        PluginsInitializeInfo plugins = PluginsLoader.initPlugins(null, false);
        for (CompositeMessage message : plugins.getPluginErrors()) {
            System.out.println(message.toString());
        }
    }
}
