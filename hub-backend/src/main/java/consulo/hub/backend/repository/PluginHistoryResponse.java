package consulo.hub.backend.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 19/08/2023
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties
public class PluginHistoryResponse
{
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties
	public static class PluginHistory
	{
		public String id;

		@JsonUnwrapped
		public RestPluginHistoryEntry history;
	}

	public List<PluginHistory> entries = new ArrayList<>();
}
