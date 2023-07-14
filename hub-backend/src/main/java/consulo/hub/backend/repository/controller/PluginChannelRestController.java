package consulo.hub.backend.repository.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.backend.repository.*;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.PlatformNodeDesc;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 24-Sep-16
 */
@RestController
public class PluginChannelRestController
{
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	private static class NotAuthorizedException extends RuntimeException
	{
	}

	private final RepositoryChannelsService myPluginChannelsService;
	private final PluginDeployService myPluginDeployService;
	private final PluginStatisticsService myPluginStatisticsService;
	private final ObjectMapper myObjectMapper;

	@Autowired
	public PluginChannelRestController(@Nonnull RepositoryChannelsService pluginChannelsService,
									   @Nonnull PluginDeployService pluginDeployService,
									   @Nonnull PluginStatisticsService pluginStatisticsService,
									   ObjectMapper objectMapper)
	{
		myPluginChannelsService = pluginChannelsService;
		myPluginDeployService = pluginDeployService;
		myPluginStatisticsService = pluginStatisticsService;
		myObjectMapper = objectMapper;
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
									  @RequestParam(value = "noRedirect", defaultValue = "false", required = false) boolean noRedirect,
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

		RepositoryChannelStore channelService = myPluginChannelsService.getRepositoryByChannel(channel);

		String idNew = idValue;
		if(zip)
		{
			idNew = idValue + "-zip";
		}

		// FIXME [VISTALL] remap to new platform id - remove later
		PlatformNodeDesc newPlatformDesc = PlatformNodeDesc.findByOldId(idNew);
		if(newPlatformDesc != null)
		{
			idNew = newPlatformDesc.id();
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

		if(!noRedirect)
		{
			String[] downloadUrls = select.downloadUrls;
			if(downloadUrls != null && downloadUrls.length > 0)
			{
				String downloadUrl = downloadUrls[0];
				try
				{
					return ResponseEntity.status(HttpStatus.FOUND).location(new URI(downloadUrl)).build();
				}
				catch(URISyntaxException ignored)
				{
				}
			}
		}

		File targetFile = select.targetFile;
		if(targetFile != null)
		{
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + targetFile.getName() + "\"").body(new FileSystemResource(targetFile));
		}
		else
		{
			Path targetPath = Objects.requireNonNull(select.targetPath);
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + targetPath.getFileName() + "\"").body(new FileSystemResource(targetPath));
		}
	}

	@RequestMapping(value = "/api/repository/platformDeploy", method = RequestMethod.POST)
	public PluginNode platformDeploy(@RequestParam("channel") PluginChannel channel,
									 @RequestParam("file") MultipartFile file,
									 @RequestParam(value = "history", required = false) MultipartFile history,
									 @RequestParam(value = "github", required = false) MultipartFile github,
									 @RequestParam("platformVersion") int platformVersion,
									 @AuthenticationPrincipal UserAccount userAccount) throws Exception
	{
		if(!hasRole(userAccount, Roles.ROLE_SUPERDEPLOYER))
		{
			throw new NotAuthorizedException();
		}

		RestPluginGithubInfo pluginGithubInfo = null;
		if(github != null)
		{
			pluginGithubInfo = myObjectMapper.readValue(github.getBytes(), RestPluginGithubInfo.class);
		}

		return myPluginDeployService.deployPlatform(channel, pluginGithubInfo, platformVersion, file, history);
	}

	@RequestMapping(value = "/api/repository/pluginDeploy", method = RequestMethod.POST)
	public PluginNode pluginDeploy(@RequestParam("channel") PluginChannel channel,
								   @RequestParam("file") MultipartFile file,
								   @RequestParam(value = "history", required = false) MultipartFile history,
								   @RequestParam(value = "github", required = false) MultipartFile github,
								   @AuthenticationPrincipal UserAccount userAccount) throws Exception
	{
		if(!hasRole(userAccount, Roles.ROLE_SUPERDEPLOYER))
		{
			throw new NotAuthorizedException();
		}

		RestPluginGithubInfo pluginGithubInfo = null;
		if(github != null)
		{
			pluginGithubInfo = myObjectMapper.readValue(github.getBytes(), RestPluginGithubInfo.class);
		}
		return myPluginDeployService.deployPlugin(channel, () -> history == null ? null : history.getInputStream(), file::getInputStream, pluginGithubInfo);
	}

	private static boolean hasRole(UserAccount userAccount, String role)
	{
		return userAccount.getAuthorities().contains(new SimpleGrantedAuthority(role));
	}

	@RequestMapping("/api/repository/list")
	public List<PluginNode> list(@RequestParam("channel") PluginChannel channel,
								 @RequestParam("platformVersion") String platformVersion,
								 @RequestParam(value = "addObsoletePlatforms", defaultValue = "true", required = false) boolean addObsoletePlatforms,
								 @RequestParam(value = "platformBuildSelect", defaultValue = "false", required = false) boolean platformBuildSelect)
	{
		RepositoryChannelStore channelService = myPluginChannelsService.getRepositoryByChannel(channel);

		ArrayList<PluginNode> result = channelService.select(myPluginStatisticsService, platformVersion, platformBuildSelect);

		// TODO we put old style platform ids
		if(addObsoletePlatforms)
		{
			Collection<PlatformNodeDesc> values = PlatformNodeDesc.values();

			result.ensureCapacity(result.size() + values.size());

			List<PluginNode> join = new ArrayList<>(values.size());

			for(PluginNode node : result)
			{
				PlatformNodeDesc nodeDesc = PlatformNodeDesc.getNode(node.id);
				if(nodeDesc == null)
				{
					continue;
				}

				String oldId = nodeDesc.oldId();
				if(oldId == null)
				{
					continue;
				}

				PluginNode oldNode = node.clone();
				oldNode.id = oldId;
				oldNode.obsolete = true;
				join.add(oldNode);
			}

			result.addAll(join);
		}

		return result;
	}

	@RequestMapping("/api/repository/info")
	public ResponseEntity<PluginNode> info(@RequestParam("channel") PluginChannel channel,
										   @RequestParam("platformVersion") String platformVersion,
										   @RequestParam("id") final String id,
										   @RequestParam(value = "zip", defaultValue = "false", required = false) boolean zip,
										   @RequestParam(value = "version") String version)
	{
		RepositoryChannelStore channelService = myPluginChannelsService.getRepositoryByChannel(channel);

		String idNew = id;
		if(zip)
		{
			idNew = id + "-zip";
		}

		// FIXME [VISTALL] remap to new platform id - remove later
		PlatformNodeDesc newPlatformDesc = PlatformNodeDesc.findByOldId(idNew);
		if(newPlatformDesc != null)
		{
			idNew = newPlatformDesc.id();
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

	@RequestMapping("/api/repository/selectChannel")
	public ResponseEntity<PluginChannelSelect> selectChannel(@RequestParam("platformId") final String platformId, @RequestParam("platformVersion") String platformVersion)
	{
		return ResponseEntity.ok(new PluginChannelSelect());
	}
}
