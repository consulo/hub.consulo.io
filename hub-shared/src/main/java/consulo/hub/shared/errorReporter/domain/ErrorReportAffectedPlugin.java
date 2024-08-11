package consulo.hub.shared.errorReporter.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
* @author VISTALL
* @since 28/08/2021
*/
@Entity
public class ErrorReportAffectedPlugin
{
	@Id
	@GeneratedValue
	private Long id;

	private String pluginId;
	private String pluginVersion;

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public void setPluginVersion(String pluginVersion)
	{
		this.pluginVersion = pluginVersion;
	}

	public String getPluginVersion()
	{
		return pluginVersion;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public Long getId()
	{
		return id;
	}
}
