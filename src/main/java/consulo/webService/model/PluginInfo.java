package consulo.webService.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * @author VISTALL
 * @since 15.06.2015
 */
@Entity
@Table(name = "consulo_plugin_infos")
public class PluginInfo
{
	@Id
	@Column(name = "id")
	public String id;

	@Column(name = "download_count")
	public long downloadCount;

	public PluginInfo()
	{
	}

	public PluginInfo(String id)
	{
		this.id = id;
	}
}
