package consulo.webService.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

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
import com.intellij.openapi.util.io.FileUtil;
import consulo.webService.PluginChannelsService;

/**
 * @author VISTALL
 * @since 24-Sep-16
 */
@RestController
public class PluginChannelRestController
{
	private final PluginChannelsService myPluginChannelsService;
	private final PluginDeployService myPluginDeployService;

	@Autowired
	public PluginChannelRestController(PluginChannelsService pluginChannelsService, PluginDeployService pluginDeployService)
	{
		myPluginChannelsService = pluginChannelsService;
		myPluginDeployService = pluginDeployService;
	}

	@RequestMapping("/api/plugins/download")
	public ResponseEntity<?> download(@RequestParam("channel") PluginChannel channel, @RequestParam("platformVersion") String platformVersion, @RequestParam("pluginId") String pluginId)
	{
		PluginChannelService channelService = myPluginChannelsService.getUpdateService(channel);

		PluginNode select = channelService.select(platformVersion, pluginId);
		if(select == null)
		{
			return ResponseEntity.notFound().build();
		}

		File targetFile = select.targetFile;
		assert targetFile != null;
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + targetFile.getName() + "\"").body(new FileSystemResource(targetFile));
	}

	@RequestMapping(value = "/api/plugins/platformDeploy", method = RequestMethod.POST)
	public PluginNode platformDeploy(@RequestParam("channel") PluginChannel channel,
			@RequestBody(required = true) MultipartFile file,
			@RequestParam("platformVersion") int platformVersion,
			@RequestHeader("Authorization") String authorization) throws IOException
	{
		String keyFromClient = authorization;
		String keyFromFs = loadDeployKey();
		//TODO [VISTALL] removed this hack later - use oauth
		if(!Objects.equals(keyFromClient, keyFromFs))
		{
			throw new IOException("bad auth");
		}

		return myPluginDeployService.deployPlatform(channel, platformVersion, file);
	}

	@RequestMapping(value = "/api/plugins/deploy", method = RequestMethod.POST)
	public PluginNode deploy(@RequestParam("channel") PluginChannel channel, @RequestBody(required = true) MultipartFile file, @RequestHeader("Authorization") String authorization) throws IOException
	{
		String keyFromClient = authorization;
		String keyFromFs = loadDeployKey();
		//TODO [VISTALL] removed this hack later - use oauth
		if(!Objects.equals(keyFromClient, keyFromFs))
		{
			throw new IOException("bad auth");
		}

		return myPluginDeployService.deployPlugin(channel, file::getInputStream);
	}

	@RequestMapping("/api/plugins/list")
	public PluginNode[] list(@RequestParam("channel") PluginChannel channel, @RequestParam("platformVersion") String platformVersion)
	{
		PluginChannelService channelService = myPluginChannelsService.getUpdateService(channel);

		return channelService.select(platformVersion);
	}

	private String loadDeployKey() throws IOException
	{
		File file = new File(myPluginChannelsService.getConsuloWebServiceHome(), "deploy.key");
		return file.exists() ? FileUtil.loadTextAndClose(new FileInputStream(file)) : null;
	}
}
