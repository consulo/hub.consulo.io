package consulo.pluginAnalyzer;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.core.CoreApplicationEnvironment;
import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.Disposer;

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
			/*if(message != null)
			{
				System.out.println(message);
			}
			if(t != null)
			{
				t.printStackTrace();
			} */
		}
	}

	public static class SilentFactory implements Logger.Factory
	{
		@Override
		public Logger getLoggerInstance(String s)
		{
			return new SilentLogger(s);
		}
	}

	// called by reflection inside PluginAnalyzerService
	public static void before()
	{
		Logger.setFactory(SilentFactory.class);

		// we need create app, and disable UnitTest mode, some plugins check it in fileTypeFactory
		new CoreApplicationEnvironment(Disposer.newDisposable())
		{
			@NotNull
			@Override
			protected MockApplication createApplication(@NotNull Disposable parentDisposable)
			{
				return new MockApplication(parentDisposable)
				{
					@Override
					public boolean isUnitTestMode()
					{
						return false;
					}
				};
			}
		};
	}

	// called by reflection inside PluginAnalyzerService
	public static void analyzeFileType(Set<String> extensions, FileTypeFactory fileTypeFactory)
	{
		CollectFileTypeConsumer consumer = new CollectFileTypeConsumer(extensions);

		fileTypeFactory.createFileTypes(consumer);
	}
}
