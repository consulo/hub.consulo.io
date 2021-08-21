package consulo.hub.backend.repository;

import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 24-Sep-16
 */
@RestController
public class PluginChannelRestController
{
	private final PluginChannelsService myUserConfigurationService;
	private final PluginDeployService myPluginDeployService;
	private final PluginStatisticsService myPluginStatisticsService;

	@Autowired
	public PluginChannelRestController(@Nonnull PluginChannelsService userConfigurationService,
			@Nonnull PluginDeployService pluginDeployService,
			@Nonnull PluginStatisticsService pluginStatisticsService)
	{
		myUserConfigurationService = userConfigurationService;
		myPluginDeployService = pluginDeployService;
		myPluginStatisticsService = pluginStatisticsService;
	}

	// api methods

	@RequestMapping("/api/repository/download")
	public ResponseEntity<?> download(@RequestParam("channel") PluginChannel channel,
			@RequestParam("platformVersion") String platformVersion,
			@Deprecated @RequestParam(value = "pluginId", required = false) final String pluginId,
			@RequestParam(value = "id", required = false /* TODO [VISTALL] remove it after dropping 'pluginId' parameter*/) final String id,
			@RequestParam(value = "noTracking", defaultValue = "false", required = false) boolean noTracking,
			@RequestParam(value = "platformBuildSelect", defaultValue = "false", required = false) boolean platformBuildSelect,
			@RequestParam(value = "zip", defaultValue = "false", required = false) boolean zip,
			@RequestParam(value = "viaUpdate", defaultValue = "false", required = false) boolean viaUpdate,
			@RequestParam(value = "version", required = false) String version)
	{
		if(id == null && pluginId == null)
		{
			throw new IllegalArgumentException("'id' is null");
		}

		String idValue = id;
		if(pluginId != null)
		{
			idValue = pluginId;
		}

		PluginChannelService channelService = myUserConfigurationService.getRepositoryByChannel(channel);

		String idNew = idValue;
		if(zip)
		{
			idNew = idValue + "-zip";
		}

		PluginNode select = channelService.select(platformVersion, idNew, version, platformBuildSelect);
		if(select == null)
		{
			idNew = idValue;
			select = channelService.select(platformVersion, idNew, version, platformBuildSelect);
		}

		if(select == null)
		{
			return ResponseEntity.notFound().build();
		}

		if(!noTracking)
		{
			myPluginStatisticsService.increaseDownload(idNew, channel, select.version, platformVersion, viaUpdate);
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

	@RequestMapping("/api/repository/info")
	public ResponseEntity<PluginNode> info(@RequestParam("channel") PluginChannel channel,
			@RequestParam("platformVersion") String platformVersion,
			@RequestParam("id") final String id,
			@RequestParam(value = "zip", defaultValue = "false", required = false) boolean zip,
			@RequestParam(value = "version") String version)
	{
		PluginChannelService channelService = myUserConfigurationService.getRepositoryByChannel(channel);

		String idNew = id;
		if(zip)
		{
			idNew = id + "-zip";
		}

		PluginNode select = channelService.select(platformVersion, idNew, version, true);
		if(select == null)
		{
			idNew = id;
			select = channelService.select(platformVersion, idNew, version, true);
		}

		if(select == null)
		{
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		return ResponseEntity.ok(select.clone());
	}

	@Nullable
	private String getDeployKey()
	{
		// todo
		return null;
	}
}
