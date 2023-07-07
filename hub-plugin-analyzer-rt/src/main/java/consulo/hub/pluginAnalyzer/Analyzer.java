package consulo.hub.pluginAnalyzer;

import consulo.application.ApplicationManager;
import consulo.application.impl.internal.plugin.CompositeMessage;
import consulo.application.impl.internal.plugin.PluginsInitializeInfo;
import consulo.application.impl.internal.plugin.PluginsLoader;
import consulo.component.extension.ExtensionPoint;
import consulo.component.extension.preview.ExtensionPreview;
import consulo.component.extension.preview.ExtensionPreviewRecorder;
import consulo.component.internal.inject.InjectingBindingLoader;
import consulo.container.plugin.PluginDescriptor;
import consulo.container.plugin.PluginDescriptorStatus;
import consulo.container.plugin.PluginManager;
import consulo.disposer.Disposable;
import consulo.hub.pluginAnalyzer.logger.SilentLoggerFactory;
import consulo.logging.internal.LoggerFactoryInitializer;

import java.util.*;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class Analyzer
{
	private static Disposable ourRootDisposable = Disposable.newDisposable();

	// called by reflection inside PluginAnalyzerService
	public static List<Map<String, String>> before(String targetPluginId)
	{
		LoggerFactoryInitializer.setFactory(new SilentLoggerFactory());

		initOtherPlugins();

		InjectingBindingLoader injectingBindingLoader = InjectingBindingLoader.INSTANCE;

		injectingBindingLoader.analyzeBindings();

		AnalyzerApplication application = new AnalyzerApplication(ourRootDisposable);
		ApplicationManager.setApplication(application, ourRootDisposable);

		ExtensionPoint<ExtensionPreviewRecorder> recorders = application.getExtensionPoint(ExtensionPreviewRecorder.class);

		List<ExtensionPreview> previews = new ArrayList<>();

		recorders.forEachExtensionSafe(extensionPreviewRecorder -> extensionPreviewRecorder.analyze(it ->
		{
			ExtensionPreview extensionPreview = (ExtensionPreview) it;

			if(!targetPluginId.equals(extensionPreview.getImplPluginId().getIdString()))
			{
				return;
			}

			previews.add(extensionPreview);
		}));

		if(previews.isEmpty())
		{
			return List.of();
		}

		List<Map<String, String>> result = new LinkedList<>();
		for(ExtensionPreview preview : previews)
		{
			Map<String, String> map = new HashMap<>();
			map.put("apiClassName", preview.getApiClassName());
			map.put("apiPluginId", preview.getApiPluginId().toString());
			map.put("implId", preview.getImplId());
			map.put("implPluginId", preview.getImplPluginId().toString());
			result.add(map);
		}
		return result;
	}

	private static void initOtherPlugins()
	{
		PluginsInitializeInfo plugins = PluginsLoader.initPlugins(null, false);
		for(CompositeMessage message : plugins.getPluginErrors())
		{
			//System.out.println(message.toString());
		}
	}

	public static void after()
	{
		ArrayList<PluginDescriptor> descriptors = new ArrayList<>(PluginManager.getPlugins());
		ourRootDisposable.disposeWithTree();
		ourRootDisposable = null;

		for(PluginDescriptor descriptor : descriptors)
		{
			if(descriptor.getStatus() != PluginDescriptorStatus.OK)
			{
				continue;
			}

			ClassLoader pluginClassLoader = descriptor.getPluginClassLoader();
			if(pluginClassLoader == null)
			{
				continue;
			}

			if(pluginClassLoader instanceof AutoCloseable closeable)
			{
				try
				{
					closeable.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
