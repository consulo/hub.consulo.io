package consulo.webService.plugins;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import consulo.webService.UserConfigurationService;

/**
 * @author VISTALL
 * @since 03-Jan-17
 */
@Component
public class PluginChannelIteration
{
	private UserConfigurationService myUserConfigurationService;

	@Autowired
	public PluginChannelIteration(UserConfigurationService userConfigurationService)
	{
		myUserConfigurationService = userConfigurationService;
	}

	@Scheduled(cron = "0 * * * * *")
	public void cleanup()
	{
		Arrays.stream(PluginChannel.values()).parallel().forEach(this::cleanup);
	}

	private void cleanup(PluginChannel pluginChannel)
	{
		PluginChannelService pluginChannelService = myUserConfigurationService.getRepositoryByChannel(pluginChannel);
	}

	/**
	 * every hour
	 */
	@Scheduled(cron = "0 * * * * *")
	public void iterAlpha()
	{
		iterate(PluginChannel.nightly, PluginChannel.alpha);
	}

	/**
	 * every week
	 */
	@Scheduled(cron = "0 0 * * * MON")
	public void iterBeta()
	{
		iterate(PluginChannel.alpha, PluginChannel.beta);
	}

	/**
	 * every month
	 */
	@Scheduled(cron = "0 0 1 * * *")
	public void iterRelease()
	{
		iterate(PluginChannel.beta, PluginChannel.release);
	}

	private static void iterate(PluginChannel from, PluginChannel to)
	{

	}
}
