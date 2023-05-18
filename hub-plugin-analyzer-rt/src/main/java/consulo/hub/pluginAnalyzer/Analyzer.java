package consulo.hub.pluginAnalyzer;

import consulo.application.ApplicationManager;
import consulo.application.impl.internal.plugin.CompositeMessage;
import consulo.application.impl.internal.plugin.PluginsInitializeInfo;
import consulo.application.impl.internal.plugin.PluginsLoader;
import consulo.component.extension.ExtensionPoint;
import consulo.component.extension.preview.ExtensionPreview;
import consulo.component.extension.preview.ExtensionPreviewRecorder;
import consulo.component.internal.inject.InjectingBindingLoader;
import consulo.disposer.Disposable;
import consulo.logging.Logger;
import consulo.logging.internal.DefaultLogger;
import consulo.logging.internal.LoggerFactory;
import consulo.logging.internal.LoggerFactoryInitializer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class Analyzer
{
	public static class SilentLogger extends DefaultLogger
	{
		public SilentLogger(String category)
		{
			super(category);
		}

		@Override
		public void error(String message, @Nullable Throwable t, String... details)
		{
			if(message != null)
			{
				System.out.println(message);
			}
			if(t != null)
			{
				t.printStackTrace();
			}
		}
	}

	public static class SilentFactory implements LoggerFactory
	{
		@Nonnull
		@Override
		public Logger getLoggerInstance(String s)
		{
			return new SilentLogger(s);
		}

		@Nonnull
		@Override
		public Logger getLoggerInstance(@Nonnull Class<?> aClass)
		{
			return new SilentLogger(aClass.getName());
		}

		@Override
		public int getPriority()
		{
			return 0;
		}

		@Override
		public void shutdown()
		{

		}
	}

	private static Disposable ourRootDisposable = Disposable.newDisposable();

	// called by reflection inside PluginAnalyzerService
	public static List<Map<String, String>> before(String targetPluginId)
	{
		initOtherPlugins();

		LoggerFactoryInitializer.setFactory(new SilentFactory());

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
			System.out.println(message.toString());
		}
	}

	public static void after()
	{
		ourRootDisposable.disposeWithTree();
	}
}
