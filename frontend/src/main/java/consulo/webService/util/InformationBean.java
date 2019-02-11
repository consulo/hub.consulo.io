package consulo.webService.util;

/**
 * @author VISTALL
 * @since 2019-02-11
 */
public class InformationBean
{
	private String osName;
	private String osVersion;
	private String javaVersion;
	private String javaVmVendor;
	private String locale;

	private String appUpdateChannel;
	private String appBuild;
	private String appVersionMajor;
	private String appVersionMinor;
	private String appBuildDate;
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
