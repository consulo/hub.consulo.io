package consulo.hub.shared.repository;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 23/05/2023
 */
public class FrontPluginNode
{
	@JsonUnwrapped
	public PluginNode myPluginNode;

	public Set<PluginChannel> myChannels = new TreeSet<>();

	public String id()
	{
		return myPluginNode.id;
	}

	public String version()
	{
		return myPluginNode.version;
	}

	public String name()
	{
		return myPluginNode.name;
	}

	public String platformVersion()
	{
		return myPluginNode.platformVersion;
	}

	public String[] tags()
	{
		return myPluginNode.tags;
	}

	public boolean experimental()
	{
		return myPluginNode.experimental;
	}

	public PluginNode.Permission[] permissions()
	{
		return myPluginNode.permissions;
	}

	public String vendor()
	{
		return myPluginNode.vendor;
	}

	public String description()
	{
		return myPluginNode.description;
	}

	public int downloads()
	{
		return myPluginNode.downloads;
	}

	public long date()
	{
		return myPluginNode.date;
	}
}
