package consulo.hub.backend.errorReporter;

import consulo.hub.backend.errorReporter.mongo.ErrorReportAttachmentRepository;
import consulo.hub.backend.errorReporter.mongo.ErrorReportRepository;
import consulo.hub.backend.repository.PluginChannelService;
import consulo.hub.backend.repository.PluginChannelsService;
import consulo.hub.shared.ServiceAccounts;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportAttachment;
import consulo.hub.shared.errorReporter.domain.ErrorReporterStatus;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@RestController
public class ErrorReportRestController
{
	private static enum CreateResult
	{
		OK, PLATFORM_UPDATE_REQUIRED, PLUGIN_UPDATE_REQUIRED, BAD_REPORT
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
	private PluginChannelsService myUserConfigurationService;

	@RequestMapping(value = "/api/errorReporter/create", method = RequestMethod.POST)
	public Map<String, String> create(@AuthenticationPrincipal UserAccount account, @RequestBody ErrorReport errorReport) throws IOException
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

		if(account != null)
		{
			errorReport.setReporterEmail(account.getUsername());
		}
		else
		{
			errorReport.setReporterEmail(ServiceAccounts.ERROR_REPORTER);
		}

		// do not allow override it via post body
		errorReport.setChangedByEmail(null);
		errorReport.setChangeTime(null);
		errorReport.setStatus(ErrorReporterStatus.UNKNOWN);
		errorReport.setCreateDate(System.currentTimeMillis());

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
