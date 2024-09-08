package consulo.hub.pluginAnalyzer.logger;

import consulo.logging.Logger;
import consulo.logging.internal.LoggerFactory;
import jakarta.annotation.Nonnull;

/**
* @author VISTALL
* @since 06/07/2023
*/
public class SilentLoggerFactory implements LoggerFactory
{
	@Nonnull
	@Override
	public Logger getLoggerInstance(String s)
	{
		return new SilentLogger();
	}

	@Nonnull
	@Override
	public Logger getLoggerInstance(@Nonnull Class<?> aClass)
	{
		return new SilentLogger();
	}

	@Override
	public void shutdown()
	{

	}
}
