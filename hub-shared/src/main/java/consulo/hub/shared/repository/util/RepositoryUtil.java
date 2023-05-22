package consulo.hub.shared.repository.util;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
public class RepositoryUtil
{
	public static final String ourStandardWinId = "consulo.dist.windows.no.jre";
	public static final String ourStandardLinuxId = "consulo.dist.linux.no.jre";
	public static final String ourStandardMacId = "consulo.dist.mac64.no.jre";

	public static String mapFromOldPlatformId(String nodeId)
	{
		PlatformNodeDesc desc = PlatformNodeDesc.findByOldId(nodeId);
		return desc != null ? desc.id() : nodeId;
	}

	public static boolean isPlatformNode(String pluginId)
	{
		return PlatformNodeDesc.getNode(pluginId) != null;
	}

	public static String extractIdFromFileName(String fileName)
	{
		String tarGzPrefix = ".tar.gz";
		if(fileName.endsWith(tarGzPrefix))
		{
			return fileName.substring(0, fileName.length() - tarGzPrefix.length());
		}

		int i = fileName.lastIndexOf('.');
		if(i != 0)
		{
			return fileName.substring(0, i);
		}

		throw new IllegalArgumentException("Invalida fileName " + fileName);
	}
}
