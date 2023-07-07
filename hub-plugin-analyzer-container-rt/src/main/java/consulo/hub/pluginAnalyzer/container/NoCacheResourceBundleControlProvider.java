package consulo.hub.pluginAnalyzer.container;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.spi.ResourceBundleControlProvider;

/**
 * @author VISTALL
 * @since 07/07/2023
 */
public class NoCacheResourceBundleControlProvider implements ResourceBundleControlProvider
{
	@Override
	public ResourceBundle.Control getControl(String baseName)
	{
		return new ResourceBundle.Control()
		{
			@Override
			public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException
			{
				// mark reload as true - not not cache or lock !
				return super.newBundle(baseName, locale, format, loader, true);
			}

			@Override
			public long getTimeToLive(String baseName, Locale locale)
			{
				return TTL_DONT_CACHE;
			}
		};
	}
}
