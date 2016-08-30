package consulo.webService;

import java.io.File;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public abstract class ChildService
{
	private boolean myInitialized;

	protected abstract void initImpl(File pluginChannelDir);

	public String getTitle()
	{
		return "Update Service";
	}

	public void init(File pluginChannelDir)
	{
		initImpl(pluginChannelDir);

		myInitialized = true;
	}

	public boolean isInitialized()
	{
		return myInitialized;
	}
}
