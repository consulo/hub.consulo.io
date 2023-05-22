package consulo.hub.shared.repository.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 22/05/2023
 */
public record PlatformNodeDesc(String id, String name, String oldId, String ext)
{
	private static Map<String, PlatformNodeDesc> ourNodes = new HashMap<>();

	static
	{
		add(new PlatformNodeDesc("consulo.dist.linux.no.jre", "Platform (Linux, without JRE)", "consulo-linux-no-jre", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.linux", "Platform (Linux, with JRE x86)", "consulo-linux", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.linux64", "Platform (Linux, with JRE x64)", "consulo-linux64", "tar.gz"));

		add(new PlatformNodeDesc("consulo.dist.mac64.no.jre", "Platform (macOS x64, without JRE)", "consulo-mac-no-jre", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.mac64", "Platform (macOS x64, with JRE)", "consulo-mac64", "tar.gz"));

		add(new PlatformNodeDesc("consulo.dist.windows.no.jre", "Platform (Windows, without JRE)", "consulo-win-no-jre", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windows.no.jre.zip", "Platform (Windows, without JRE, zip archive)", "consulo-win-no-jre-zip", "zip"));

		add(new PlatformNodeDesc("consulo.dist.windows", "Platform (Windows, with JRE x86)", "consulo-win", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windows.zip", "Platform (Windows, zip archive)", "consulo-win-zip", "zip"));

		add(new PlatformNodeDesc("consulo.dist.windows64", "Platform (Windows, with JRE x64)", "consulo-win64", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windows64.installer", "Platform (Windows, with JRE x64, installer)", null, "exe"));
		add(new PlatformNodeDesc("consulo.dist.windows64.zip", "Platform (Windows, with JRE x64, zip archive)", "consulo-win64-zip", "zip"));

		add(new PlatformNodeDesc("consulo.dist.windowsA64", "Platform (Windows, with JRE ARM64)", "consulo-winA64", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windowsA64.zip", "Platform (Windows, with JRE ARM64, zip archive)", "consulo-winA64-zip", "zip"));
	}

	public static PlatformNodeDesc getNode(String id)
	{
		return ourNodes.get(id);
	}

	public static PlatformNodeDesc findByOldId(String oldId)
	{
		for(PlatformNodeDesc node : ourNodes.values())
		{
			if(Objects.equals(oldId, node.oldId()))
			{
				return node;
			}
		}

		return null;
	}

	private static void add(PlatformNodeDesc node)
	{
		ourNodes.put(node.id(), node);
	}
}
