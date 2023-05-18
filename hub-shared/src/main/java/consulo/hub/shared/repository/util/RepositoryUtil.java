package consulo.hub.shared.repository.util;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
public class RepositoryUtil
{
	public static final String ourStandardWinId = "consulo-win-no-jre";
	public static final String ourStandardLinuxId = "consulo-linux-no-jre";
	public static final String ourStandardMacId = "consulo-mac-no-jre";

	public static final String[] ourPlatformPluginIds = {
			ourStandardWinId,
			"consulo-win",
			"consulo-win64",
			"consulo-winA64",
			ourStandardLinuxId,
			"consulo-linux",
			"consulo-linux64",
			ourStandardMacId,
			"consulo-mac64",
			// special case for windows
			ourStandardWinId + "-zip",
			"consulo-win" + "-zip",
			"consulo-win64" + "-zip",
			"consulo-winA64" + "-zip",
	};

	public static boolean isPlatformNode(String pluginId)
	{
		return ArrayUtils.contains(ourPlatformPluginIds, pluginId);
	}
}
