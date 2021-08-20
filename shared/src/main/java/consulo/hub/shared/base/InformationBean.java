package consulo.hub.shared.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author VISTALL
 * @since 2019-02-11
 */
@MappedSuperclass
public class InformationBean
{
	@Column
	private String osName;
	@Column
	private String osVersion;
	@Column
	private String javaVersion;
	@Column
	private String javaVmVendor;
	@Column
	private String locale;

	@Column
	private String appUpdateChannel;
	@Column
	private String appBuild;
	@Column
	private String appVersionMajor;
	@Column
	private String appVersionMinor;
	@Column
	private String appBuildDate;
	@Column
	private boolean appIsInternal;

	public void copyTo(InformationBean bean)
	{
		bean.setOsName(getOsName());
		bean.setOsVersion(getOsVersion());
		bean.setJavaVersion(getJavaVersion());
		bean.setJavaVmVendor(getJavaVmVendor());
		bean.setLocale(getLocale());

		bean.setAppUpdateChannel(getAppUpdateChannel());
		bean.setAppBuild(getAppBuild());
		bean.setAppVersionMajor(getAppVersionMajor());
		bean.setAppVersionMinor(getAppVersionMinor());
		bean.setAppBuildDate(getAppBuildDate());
		bean.setAppIsInternal(isAppIsInternal());
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
}
