package consulo.hub.backend.repository.analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import consulo.hub.pluginAnalyzer.container.ContainerBoot;
import consulo.hub.shared.repository.PluginNode;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class PluginAnalyzerRunner
{
	private final PluginAnalyzerEnv myEnv;
	private final ObjectMapper myObjectMapper;

	public PluginAnalyzerRunner(PluginAnalyzerEnv env, ObjectMapper objectMapper)
	{
		myEnv = env;
		myObjectMapper = objectMapper;
	}

	public PluginNode.ExtensionPreview[] run(String targetPluginId, String[] pluginsDir) throws Exception
	{
		List<Map<String, String>> maps = run(myEnv.getPlatformClassGroup().getClassUrls(), myEnv.getAnalyzerClassGroup().getClassUrls(), pluginsDir, targetPluginId);

		return myObjectMapper.convertValue(maps, PluginNode.ExtensionPreview[].class);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, String>> run(List<URL> platformURLs, List<URL> analyzerURLs, String[] pluginsDir, String targetPluginId) throws Exception
	{
		try (URLClassLoader containerClassLoader = new URLClassLoader(myEnv.getContainerGroup().getClassUrls().toArray(URL[]::new), ClassLoader.getPlatformClassLoader()))
		{
			Class<?> bootClass = Class.forName(ContainerBoot.class.getName(), true, containerClassLoader);
			Method initMethod = bootClass.getDeclaredMethod("init", List.class, List.class, String[].class, String.class);
			initMethod.setAccessible(true);
			return (List<Map<String, String>>) initMethod.invoke(null, platformURLs, analyzerURLs, pluginsDir, targetPluginId);
		}
	}
}
