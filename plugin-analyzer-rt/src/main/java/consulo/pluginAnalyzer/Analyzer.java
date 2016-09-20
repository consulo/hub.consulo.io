package consulo.pluginAnalyzer;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeFactory;

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
			//
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

	public static void before()
	{
		Logger.setFactory(SilentFactory.class);
	}

	public static void analyzeFileType(Set<String> extensions, FileTypeFactory fileTypeFactory)
	{
		CollectFileTypeConsumer consumer = new CollectFileTypeConsumer(extensions);

		fileTypeFactory.createFileTypes(consumer);
	}
}
