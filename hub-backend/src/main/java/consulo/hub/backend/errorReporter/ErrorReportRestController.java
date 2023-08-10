package consulo.hub.backend.errorReporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.auth.repository.UserAccountRepository;
import consulo.hub.backend.errorReporter.repository.ErrorReportRepository;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.errorReporter.domain.ErrorReport;
import consulo.hub.shared.errorReporter.domain.ErrorReportAffectedPlugin;
import consulo.hub.shared.errorReporter.domain.ErrorReportAttachment;
import consulo.hub.shared.errorReporter.domain.ErrorReportStatus;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.hub.shared.util.JsonPage;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@RestController
public class ErrorReportRestController
{
	private static final Logger LOG = LoggerFactory.getLogger(ErrorReportRestController.class);

	private static enum CreateResult
	{
		OK,
		PLATFORM_UPDATE_REQUIRED,
		PLUGIN_UPDATE_REQUIRED,
		BAD_REPORT
	}

	private static enum OS
	{
		win(RepositoryUtil.ourStandardWinId),
		linux(RepositoryUtil.ourStandardLinuxId),
		mac(RepositoryUtil.ourStandardMacId);

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
	private UserAccountRepository myUserAccountRepository;

	@Autowired
	private RepositoryChannelsService myPluginChannelsService;

	@Autowired
	private ObjectMapper myObjectMapper;

	@Autowired
	private WorkDirectoryService myWorkDirectoryService;

	@RequestMapping("/api/errorReporter/list")
	public JsonPage<ErrorReport> listErrorReports(@AuthenticationPrincipal @NonNull UserAccount account,
												  @RequestParam(value = "statuses", required = false) String statuses,
												  @RequestParam("pageSize") int pageSize,
												  @RequestParam("page") int page)
	{
		ErrorReportStatus[] selectStatuses = ErrorReportStatus.values();

		if(!StringUtils.isBlank(statuses))
		{
			selectStatuses = Arrays.stream(statuses.split(",")).map(ErrorReportStatus::valueOf).toArray(ErrorReportStatus[]::new);
		}

		Page<ErrorReport> reports;

		reports = myErrorReportRepository.findByUserAndStatusIn(account, selectStatuses, PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "createDate")));

		return new JsonPage<>(reports);
	}

	@RequestMapping(value = "/api/errorReporter/create", method = RequestMethod.POST)
	public Map<String, String> create(@AuthenticationPrincipal UserAccount account,
									  @RequestParam(value = "assignUserId", required = false) Long assignUserId,
									  @RequestBody ErrorReport errorReport) throws IOException
	{
		try
		{
			return createImpl(account, assignUserId, errorReport);
		}
		catch(Throwable e)
		{
			Path errorReportsDir = myWorkDirectoryService.getWorkingDirectory().resolve("errorReports");
			if(!Files.exists(errorReportsDir))
			{
				Files.createDirectory(errorReportsDir);
			}

			Path errorDataFile = errorReportsDir.resolve("errorReportError" + e.hashCode() + "_" + System.currentTimeMillis() + ".txt");
			String data = myObjectMapper.writeValueAsString(errorReport);
			Files.writeString(errorDataFile, data);
			return resultWithMessage(CreateResult.BAD_REPORT, null, e.getMessage());
		}
	}

	private Map<String, String> createImpl(UserAccount account, Long assignUserId, ErrorReport errorReport)
	{
		if(Boolean.TRUE)
		{
			throw new IllegalArgumentException();
		}

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

		String stackTrace = errorReport.getStackTrace();
		if(stackTrace == null)
		{
			return resultWithMessage(CreateResult.BAD_REPORT, null, "'stackTrace' required");
		}

		PluginChannel pluginChannel = PluginChannel.valueOf(appUpdateChannel);

		RepositoryChannelStore repository = myPluginChannelsService.getRepositoryByChannel(pluginChannel);

		String osName = errorReport.getOsName();
		if(osName == null)
		{
			return resultWithMessage(CreateResult.BAD_REPORT, null, "'osName' required");
		}

		OS os = OS.find(osName);

		String platformPluginId = os.myPluginId;

		PluginNode platformLastNode = repository.select(RepositoryChannelStore.SNAPSHOT, platformPluginId, null, false);

		int platformVersion = appBuild.equals(RepositoryChannelStore.SNAPSHOT) ? Integer.MAX_VALUE : Integer.parseInt(appBuild);
		if(platformLastNode != null)
		{
			int lastVersion = Integer.parseInt(platformLastNode.version);
			if(platformVersion < lastVersion)
			{
				return resultWithMessage(CreateResult.PLATFORM_UPDATE_REQUIRED, null, null);
			}
		}

		List<ErrorReportAffectedPlugin> affectedPlugins = errorReport.getAffectedPlugins();
		for(ErrorReportAffectedPlugin entry : affectedPlugins)
		{
			entry.setId(null);

			// snapshot ignore version check
			if("SNAPSHOT".equals(entry.getPluginVersion()))
			{
				continue;
			}

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

		UserAccount assignUser = null;
		if(assignUserId != null && assignUserId != 0)
		{
			assignUser = myUserAccountRepository.findById(assignUserId).get();
		}

		errorReport.setUser(account);
		errorReport.setAssignUser(assignUser);

		// limit message+description - see jpa annotations
		limitString(errorReport::getMessage, errorReport::setMessage, 1024);
		limitString(errorReport::getDescription, errorReport::setDescription, 2048);
		limitString(errorReport::getStackTrace, errorReport::setStackTrace, ErrorReport.MAX_STACKTRACE_LENGHT);

		// do not allow override it via post body
		errorReport.setChangedByUser(null);
		errorReport.setId(null);
		errorReport.setChangeTime(null);
		errorReport.setStatus(ErrorReportStatus.UNKNOWN);
		errorReport.setCreateDate(System.currentTimeMillis());

		for(ErrorReportAttachment attachment : errorReport.getAttachments())
		{
			attachment.setId(null);
		}

		errorReport.setLongId(RandomStringUtils.randomAlphanumeric(48));

		try
		{
			errorReport = myErrorReportRepository.save(errorReport);
		}
		catch(Exception e)
		{
			LOG.error("Fail to report " + new Gson().toJson(errorReport), e);
			return resultWithMessage(CreateResult.BAD_REPORT, null, null);
		}

		return resultWithMessage(CreateResult.OK, errorReport.getLongId(), null);
	}

	private void limitString(Supplier<String> getter, Consumer<String> setter, int limit)
	{
		limit--; // just be sure

		String value = getter.get();
		if(value == null)
		{
			return;
		}

		if(value.length() > limit)
		{
			setter.accept(value.substring(0, limit));
		}
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
