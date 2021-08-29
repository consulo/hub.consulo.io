package consulo.hub.shared.errorReporter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import consulo.hub.shared.auth.domain.UserAccount;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@SuppressWarnings("unused")
@JsonIgnoreProperties
@Entity
@Table(
		indexes = @Index(columnList = "longId", unique = true)
)
public class ErrorReport
{
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private Long id;

	private String longId;

	private String osName;
	private String osVersion;
	private String javaVersion;
	private String javaVmVendor;
	private String locale;

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

	@OneToMany(cascade = CascadeType.ALL)
	@OrderColumn
	private List<ErrorReportAffectedPlugin> affectedPlugins = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL)
	@OrderColumn
	private List<ErrorReportAttachment> attachments = new ArrayList<>();

	@OneToOne
	private UserAccount user;

	@OneToOne
	private UserAccount changedByUser;

	private Long changeTime;

	private ErrorReportStatus status = ErrorReportStatus.UNKNOWN;

	@Column(length = 51200)
	private String stackTrace;

	private long createDate;

	public ErrorReport()
	{
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getOsName()
	{
		return osName;
	}

	public void setOsName(String osName)
	{
		this.osName = osName;
	}

	public String getOsVersion()
	{
		return osVersion;
	}

	public void setOsVersion(String osVersion)
	{
		this.osVersion = osVersion;
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

	public String getLocale()
	{
		return locale;
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
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

	public void setUser(UserAccount user)
	{
		this.user = user;
	}

	public UserAccount getUser()
	{
		return user;
	}

	public void setChangedByUser(UserAccount changedByUser)
	{
		this.changedByUser = changedByUser;
	}

	public UserAccount getChangedByUser()
	{
		return changedByUser;
	}

	public Long getChangeTime()
	{
		return changeTime;
	}

	public void setChangeTime(Long changeTime)
	{
		this.changeTime = changeTime;
	}

	public ErrorReportStatus getStatus()
	{
		return status;
	}

	public void setStatus(ErrorReportStatus status)
	{
		this.status = status;
	}

	public String getStackTrace()
	{
		return stackTrace;
	}

	public void setStackTrace(String stackTrace)
	{
		this.stackTrace = stackTrace;
	}

	public long getCreateDate()
	{
		return createDate;
	}

	public void setCreateDate(long createDate)
	{
		this.createDate = createDate;
	}

	public List<ErrorReportAffectedPlugin> getAffectedPlugins()
	{
		return affectedPlugins;
	}

	public void setAffectedPlugins(List<ErrorReportAffectedPlugin> affectedPlugins)
	{
		this.affectedPlugins = affectedPlugins;
	}

	public List<ErrorReportAttachment> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<ErrorReportAttachment> attachments)
	{
		this.attachments = attachments;
	}

	public String getLongId()
	{
		return longId;
	}

	public void setLongId(String longId)
	{
		this.longId = longId;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		ErrorReport that = (ErrorReport) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
