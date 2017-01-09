package consulo.webService.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import consulo.webService.UserConfigurationService;
import consulo.webService.util.PropertyKeys;

/**
 * @author VISTALL
 * @since 24-Sep-16
 */
@RestController
public class PluginChannelRestController
{
	private final UserConfigurationService myUserConfigurationService;
	private final PluginDeployService myPluginDeployService;
	private final PluginStatisticsService myPluginStatisticsService;

	@Autowired
	public PluginChannelRestController(@NotNull UserConfigurationService userConfigurationService, @NotNull PluginDeployService pluginDeployService, @NotNull PluginStatisticsService pluginStatisticsService)
	{
		myUserConfigurationService = userConfigurationService;
		myPluginDeployService = pluginDeployService;
		myPluginStatisticsService = pluginStatisticsService;
	}

	// api methods

	@RequestMapping("/api/repository/download")
	public ResponseEntity<?> download(@RequestParam("channel") PluginChannel channel,
			@RequestParam("platformVersion") String platformVersion,
			@RequestParam("pluginId") String pluginId,
			@RequestParam(value = "noTracking", defaultValue = "false", required = false) boolean noTracking,
			@RequestParam(value = "platformBuildSelect", defaultValue = "false", required = false) boolean platformBuildSelect,
			@RequestParam(value = "zip", defaultValue = "false", required = false) boolean zip,
			@RequestParam(value = "version", required = false) String version)
	{
		PluginChannelService channelService = myUserConfigurationService.getRepositoryByChannel(channel);

		String pluginIdNew = pluginId;
		if(zip)
		{
			pluginIdNew = pluginId + "-zip";
		}

		PluginNode select = channelService.select(platformVersion, pluginIdNew, version, platformBuildSelect);
		if(select == null)
		{
			select = channelService.select(platformVersion, pluginId, version, platformBuildSelect);
		}

		if(select == null)
		{
			return ResponseEntity.notFound().build();
		}

		if(!noTracking)
		{
			myPluginStatisticsService.increaseDownload(pluginIdNew, channel, select.version, platformVersion);
		}

		File targetFile = select.targetFile;
		assert targetFile != null;
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + targetFile.getName() + "\"").body(new FileSystemResource(targetFile));
	}

	@RequestMapping(value = "/api/repository/platformDeploy", method = RequestMethod.POST)
	public PluginNode platformDeploy(@RequestParam("channel") PluginChannel channel,
			@RequestBody(required = true) MultipartFile file,
			@RequestParam("platformVersion") int platformVersion,
			@RequestHeader("Authorization") String authorization) throws Exception
	{
		String keyFromClient = authorization;
		String keyFromFs = getDeployKey();
		//TODO [VISTALL] removed this hack later - use oauth
		if(!Objects.equals(keyFromClient, keyFromFs))
		{
			throw new IOException("bad auth");
		}

		return myPluginDeployService.deployPlatform(channel, platformVersion, file);
	}

	@RequestMapping(value = "/api/repository/pluginDeploy", method = RequestMethod.POST)
	public PluginNode pluginDeploy(@RequestParam("channel") PluginChannel channel,
			@RequestBody(required = true) MultipartFile file,
			@RequestHeader("Authorization") String authorization) throws Exception
	{
		String keyFromClient = authorization;
		String keyFromFs = getDeployKey();
		//TODO [VISTALL] removed this hack later - use oauth
		if(!Objects.equals(keyFromClient, keyFromFs))
		{
			throw new IOException("bad auth");
		}

		return myPluginDeployService.deployPlugin(channel, file::getInputStream);
	}

	@RequestMapping("/api/repository/list")
	public PluginNode[] list(@RequestParam("channel") PluginChannel channel,
			@RequestParam("platformVersion") String platformVersion,
			@RequestParam(value = "platformBuildSelect", defaultValue = "false", required = false) boolean platformBuildSelect)
	{
		PluginChannelService channelService = myUserConfigurationService.getRepositoryByChannel(channel);

		return channelService.select(myPluginStatisticsService, platformVersion, platformBuildSelect);
	}

	@Nullable
	private String getDeployKey()
	{
		return myUserConfigurationService.getPropertySet().getStringProperty(PropertyKeys.DEPLOY_KEY);
	}
}
