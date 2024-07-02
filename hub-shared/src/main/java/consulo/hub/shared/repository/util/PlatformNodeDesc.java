package consulo.hub.shared.repository.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22/05/2023
 */
public record PlatformNodeDesc(String id, String name, String oldId, String ext)
{
	private static Map<String, PlatformNodeDesc> ourNodes = new HashMap<>();
	private static Map<String, PlatformNodeDesc> ourOldMapping = new HashMap<>();

	static
	{
		add(new PlatformNodeDesc("consulo.dist.linux.no.jre", "Platform (Linux, without JRE)", "consulo-linux-no-jre", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.linux", "Platform (Linux, with JRE x86)", "consulo-linux", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.linux64", "Platform (Linux, with JRE x64)", "consulo-linux64", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.linuxA64", "Platform (Linux, with JRE ARM64)", null, "tar.gz"));

		add(new PlatformNodeDesc("consulo.dist.mac64.no.jre", "Platform (macOS x64, without JRE)", "consulo-mac-no-jre", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.mac64", "Platform (macOS x64, with JRE)", "consulo-mac64", "tar.gz"));

		add(new PlatformNodeDesc("consulo.dist.macA64.no.jre", "Platform (macOS ARM64, without JRE)", null, "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.macA64", "Platform (macOS ARM64, with JRE)", null, "tar.gz"));

		add(new PlatformNodeDesc("consulo.dist.windows.no.jre", "Platform (Windows, without JRE)", "consulo-win-no-jre", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windows.no.jre.zip", "Platform (Windows, without JRE, zip archive)", "consulo-win-no-jre-zip", "zip"));

		add(new PlatformNodeDesc("consulo.dist.windows", "Platform (Windows, with JRE x86)", "consulo-win", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windows.zip", "Platform (Windows, zip)", "consulo-win-zip", "zip"));

		add(new PlatformNodeDesc("consulo.dist.windows64", "Platform (Windows, with JRE x64)", "consulo-win64", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windows64.installer", "Platform (Windows, with JRE x64, installer)", null, "exe"));
		add(new PlatformNodeDesc("consulo.dist.windows64.zip", "Platform (Windows, with JRE x64, zip)", "consulo-win64-zip", "zip"));

		add(new PlatformNodeDesc("consulo.dist.windowsA64", "Platform (Windows, with JRE ARM64)", "consulo-winA64", "tar.gz"));
		add(new PlatformNodeDesc("consulo.dist.windowsA64.zip", "Platform (Windows, with JRE ARM64, zip)", "consulo-winA64-zip", "zip"));
	}

	public static PlatformNodeDesc getNode(String id)
	{
		return ourNodes.get(id);
	}

	public static Collection<PlatformNodeDesc> values()
	{
		return ourNodes.values();
	}

	public static PlatformNodeDesc findByOldId(String oldId)
	{
		return ourOldMapping.get(oldId);
	}

	private static void add(PlatformNodeDesc node)
	{
		if(ourNodes.put(node.id(), node) != null)
		{
			throw new IllegalArgumentException("Duplicate " + node.id());
		}

		String oldId = node.oldId();
		if(oldId != null)
		{
			ourOldMapping.put(oldId, node);
		}
	}
}
