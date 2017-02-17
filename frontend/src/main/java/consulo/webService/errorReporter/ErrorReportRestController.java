package consulo.webService.errorReporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import consulo.webService.ServiceConstants;
import consulo.webService.UserConfigurationService;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;
import consulo.webService.auth.oauth2.mongo.OAuth2AccessTokenRepository;
import consulo.webService.errorReporter.domain.ErrorReport;
import consulo.webService.errorReporter.domain.ErrorReportAttachment;
import consulo.webService.errorReporter.mongo.ErrorReportAttachmentRepository;
import consulo.webService.errorReporter.mongo.ErrorReportRepository;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.plugins.PluginNode;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@RestController
public class ErrorReportRestController
{
	private static enum CreateResult
	{
		OK, PLATFORM_UPDATE_REQUIRED, PLUGIN_UPDATE_REQUIRED, BAD_REPORT, BAD_OAUTHK_KEY
	}

	private static enum OS
	{
		win(PluginChannelService.ourStandardWinId), linux(PluginChannelService.ourStandardLinuxId), mac(PluginChannelService.ourStandardMacId);

		private String myPluginId;

		OS(String pluginId)
		{
			myPluginId = pluginId;
		}

		public static OS find(String osProperty)
		{
			String osName = osProperty.toLowerCase();
			if(osName.startsWith("windows"))
			{
				return win;
			}

			if(osName.startsWith("os/2") || osName.startsWith("os2"))
			{
				return mac;
			}

			return linux;
		}
	}

	@Autowired
	private ErrorReportRepository myErrorReportRepository;

	@Autowired
	private ErrorReportAttachmentRepository myErrorReportAttachmentRepository;

	@Autowired
	private UserConfigurationService myUserConfigurationService;

	@Autowired
	private OAuth2AccessTokenRepository myOAuth2AccessTokenRepository;

	@RequestMapping(value = "/api/errorReporter/create", method = RequestMethod.POST)
	public Map<String, String> create(@RequestHeader(value = "Authorization", required = false) String authorizationKey, @RequestBody ErrorReport errorReport) throws IOException
	{
		String appBuild = errorReport.getAppBuild();
		if(appBuild == null)
		{
			return resultWithMessage(CreateResult.BAD_REPORT, null, "'appBuild' required");
		}

		String appUpdateChannel = errorReport.getAppUpdateChannel();
		if(appUpdateChannel == null)
		{
			return resultWithMessage(CreateResult.BAD_REPORT, null, "'appUpdateChannel' required");
		}

		PluginChannel pluginChannel = PluginChannel.valueOf(appUpdateChannel);

		PluginChannelService repository = myUserConfigurationService.getRepositoryByChannel(pluginChannel);

		String osName = errorReport.getOsName();
		if(osName == null)
		{
			return resultWithMessage(CreateResult.BAD_REPORT, null, "'osName' required");
		}

		OS os = OS.find(osName);

		String platformPluginId = os.myPluginId;

		PluginNode platformLastNode = repository.select(PluginChannelService.SNAPSHOT, platformPluginId, null, false);

		int platformVersion = appBuild.equals(PluginChannelService.SNAPSHOT) ? Integer.MAX_VALUE : Integer.parseInt(appBuild);
		if(platformLastNode != null)
		{
			int lastVersion = Integer.parseInt(platformLastNode.version);
			if(platformVersion < lastVersion)
			{
				return resultWithMessage(CreateResult.PLATFORM_UPDATE_REQUIRED, null, null);
			}
		}

		ErrorReport.AffectedPlugin[] affectedPlugins = errorReport.getAffectedPlugins();
		for(ErrorReport.AffectedPlugin entry : affectedPlugins)
		{
			int pluginVersion = Integer.parseInt(entry.getPluginVersion());

			PluginNode pluginNode = repository.select(appBuild, entry.getPluginId(), null, false);
			// if we don't have plugin at our repository - skip it
			if(pluginNode == null)
			{
				continue;
			}

			int lastPluginVersion = Integer.parseInt(pluginNode.version);
			if(pluginVersion < lastPluginVersion)
			{
				return resultWithMessage(CreateResult.PLUGIN_UPDATE_REQUIRED, null, entry.getPluginId());
			}
		}

		if(authorizationKey != null)
		{
			OAuth2AuthenticationAccessToken token = myOAuth2AccessTokenRepository.findByTokenId(authorizationKey);
			if(token == null)
			{
				return resultWithMessage(CreateResult.BAD_OAUTHK_KEY, errorReport.getId(), null);
			}

			errorReport.setReporterEmail(token.getUserName());
		}
		else
		{
			errorReport.setReporterEmail(ServiceConstants.ourBotEmail);
		}

		errorReport = myErrorReportRepository.save(errorReport);
		for(ErrorReportAttachment attachment : errorReport.getAttachments())
		{
			myErrorReportAttachmentRepository.save(attachment);
		}

		return resultWithMessage(CreateResult.OK, errorReport.getId(), null);
	}

	private static Map<String, String> resultWithMessage(CreateResult result, String id, String message)
	{
		Map<String, String> map = new HashMap<>(1);
		map.put("type", result.name());
		if(id != null)
		{
			map.put("id", id);
		}
		if(message != null)
		{
			map.put("message", message);
		}
		return map;
	}
}
