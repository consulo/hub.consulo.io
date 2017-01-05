package consulo.webService.errorReporter.domain;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author VISTALL
 * @since 02-Oct-16
 */
@SuppressWarnings("unused")
@Document(collection = "errorReport")
public class ErrorReport implements Serializable
{
	@Id
	private final String id = UUID.randomUUID().toString();

	private String osName;
	private String javaVersion;
	private String javaVmVendor;

	private String appName;
	// do not use PluginChannel
	private String appUpdateChannel;
	private String appBuild;
	private String appVersionMajor;
	private String appVersionMinor;
	private String appBuildDate;
	private boolean appIsInternal;

	private String lastAction;
	private String previousException;
	private String message;
	private String description;
	private Integer assigneeId;
	private Map<String, String> affectedPluginIds;

	@DBRef
	private ErrorReportAttachment[] attachments;

	// status fields
	@Indexed
	private String reporterEmail;
	@Indexed
	private String changedByEmail;

	private Long changeTime;

	private ErrorReporterStatus status = ErrorReporterStatus.UNKNOWN;

	private String stackTrace;

	private long createDate = System.currentTimeMillis();

	public ErrorReport()
	{
	}

	public long getCreateDate()
	{
		return createDate;
	}

	public void setCreateDate(long createDate)
	{
		this.createDate = createDate;
	}

	public String getId()
	{
		return id;
	}

	public ErrorReporterStatus getStatus()
	{
		return status;
	}

	public void setStatus(ErrorReporterStatus status)
	{
		this.status = status;
	}

	public String getReporterEmail()
	{
		return reporterEmail;
	}

	public void setReporterEmail(String reporterEmail)
	{
		this.reporterEmail = reporterEmail;
	}

	public Long getChangeTime()
	{
		return changeTime;
	}

	public void setChangeTime(Long changeTime)
	{
		this.changeTime = changeTime;
	}

	public String getChangedByEmail()
	{
		return changedByEmail;
	}

	public void setChangedByEmail(String changedByEmail)
	{
		this.changedByEmail = changedByEmail;
	}

	public String getOsName()
	{
		return osName;
	}

	public void setOsName(String osName)
	{
		this.osName = osName;
	}

	public String getJavaVersion()
	{
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion)
	{
		this.javaVersion = javaVersion;
	}

	public String getJavaVmVendor()
	{
		return javaVmVendor;
	}

	public void setJavaVmVendor(String javaVmVendor)
	{
		this.javaVmVendor = javaVmVendor;
	}

	public String getAppName()
	{
		return appName;
	}

	public void setAppName(String appName)
	{
		this.appName = appName;
	}

	public String getAppUpdateChannel()
	{
		return appUpdateChannel;
	}

	public void setAppUpdateChannel(String appUpdateChannel)
	{
		this.appUpdateChannel = appUpdateChannel;
	}

	public String getAppBuild()
	{
		return appBuild;
	}

	public void setAppBuild(String appBuild)
	{
		this.appBuild = appBuild;
	}

	public String getAppVersionMajor()
	{
		return appVersionMajor;
	}

	public void setAppVersionMajor(String appVersionMajor)
	{
		this.appVersionMajor = appVersionMajor;
	}

	public String getAppVersionMinor()
	{
		return appVersionMinor;
	}

	public void setAppVersionMinor(String appVersionMinor)
	{
		this.appVersionMinor = appVersionMinor;
	}

	public String getAppBuildDate()
	{
		return appBuildDate;
	}

	public void setAppBuildDate(String appBuildDate)
	{
		this.appBuildDate = appBuildDate;
	}

	public boolean isAppIsInternal()
	{
		return appIsInternal;
	}

	public void setAppIsInternal(boolean appIsInternal)
	{
		this.appIsInternal = appIsInternal;
	}

	public String getLastAction()
	{
		return lastAction;
	}

	public void setLastAction(String lastAction)
	{
		this.lastAction = lastAction;
	}

	public String getPreviousException()
	{
		return previousException;
	}

	public void setPreviousException(String previousException)
	{
		this.previousException = previousException;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getStackTrace()
	{
		return stackTrace;
	}

	public void setStackTrace(String stackTrace)
	{
		this.stackTrace = stackTrace;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Integer getAssigneeId()
	{
		return assigneeId;
	}

	public void setAssigneeId(Integer assigneeId)
	{
		this.assigneeId = assigneeId;
	}

	public Map<String, String> getAffectedPluginIds()
	{
		return affectedPluginIds;
	}

	public void setAffectedPluginIds(Map<String, String> affectedPluginIds)
	{
		this.affectedPluginIds = affectedPluginIds;
	}

	public ErrorReportAttachment[] getAttachments()
	{
		return attachments;
	}

	public void setAttachments(ErrorReportAttachment[] attachments)
	{
		this.attachments = attachments;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(!(o instanceof ErrorReport))
		{
			return false;
		}

		ErrorReport that = (ErrorReport) o;

		return id.equals(that.id);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}
