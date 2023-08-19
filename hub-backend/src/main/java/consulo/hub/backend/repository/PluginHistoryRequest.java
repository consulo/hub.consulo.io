package consulo.hub.backend.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author VISTALL
 * @since 19/08/2023
 */
@JsonIgnoreProperties
public class PluginHistoryRequest
{
	public static class PluginInfo
	{
		public String id;

		public String fromVersion;

		public String toVersion;

		public boolean includeFromVersion = true;
	}

	public PluginInfo[] plugins = new PluginInfo[0];
}
