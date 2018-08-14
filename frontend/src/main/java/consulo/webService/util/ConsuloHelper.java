package consulo.webService.util;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import consulo.util.logging.LoggerFactory;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class ConsuloHelper
{
	private static class MyLogger extends DefaultLogger
	{
		public MyLogger(String category)
		{
			super(category);
		}

		@Override
		public void info(String message)
		{
			infoOrWarn(message, null);
		}

		@Override
		public void info(String message, Throwable t)
		{
			infoOrWarn(message, t);
		}

		@Override
		public void warn(@NonNls String message)
		{
			infoOrWarn(message, null);
		}

		@Override
		public void warn(@NonNls String message, Throwable t)
		{
			infoOrWarn(message, t);
		}

		@Override
		public void error(String message, @Nullable Throwable t, String... details)
		{
			infoOrWarn(message, t, details);
		}

		public void infoOrWarn(String message, @Nullable Throwable t, String... details)
		{
			System.out.println(message);
			if(t != null)
			{
				t.printStackTrace();
			}
			if(details != null && details.length > 0)
			{
				System.out.println("details: ");
				for(String detail : details)
				{
					System.out.println(detail);
				}
			}
		}
	}

	private static class MyLoggerFactory implements LoggerFactory
	{
		@Nonnull
		@Override
		public Logger getLoggerInstance(String s)
		{
			return new MyLogger(s);
		}
	}

	static
	{
		Logger.setFactory(new MyLoggerFactory());
	}

	public static void init()
	{
	}
}
