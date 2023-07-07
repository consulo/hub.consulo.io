package consulo.hub.pluginAnalyzer.logger;

import consulo.logging.Logger;
import jakarta.annotation.Nullable;

/**
* @author VISTALL
* @since 06/07/2023
*/
public class SilentLogger implements Logger
{
	@Override
	public boolean isDebugEnabled()
	{
		return false;
	}

	@Override
	public void debug(String s)
	{

	}

	@Override
	public void debug(@Nullable Throwable throwable)
	{

	}

	@Override
	public void debug(String s, @Nullable Throwable throwable)
	{

	}

	@Override
	public void info(String s)
	{

	}

	@Override
	public void info(String s, @Nullable Throwable throwable)
	{

	}

	@Override
	public void warn(String s, @Nullable Throwable throwable)
	{

	}

	@Override
	public void error(String message, @Nullable Throwable t, String... details)
	{
	}
}
