package consulo.pluginAnalyzer;

import consulo.disposer.Disposable;
import consulo.logging.Logger;
import consulo.logging.internal.DefaultLogger;
import consulo.logging.internal.LoggerFactory;
import consulo.logging.internal.LoggerFactoryInitializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	public static void before()
	{
		LoggerFactoryInitializer.setFactory(new SilentFactory());

		// we need create app, and disable UnitTest mode, some plugins check it in fileTypeFactory
		//LightApplicationBuilder.create(ourRootDisposable).build();
	}


}
