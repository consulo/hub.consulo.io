package consulo.webService.update.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.ZipUtil;
import consulo.webService.RootService;
import consulo.webService.ServiceIsNotReadyException;
import consulo.webService.update.PluginChannelService;
import consulo.webService.update.PluginNode;
import consulo.webService.update.UpdateChannel;
import consulo.webService.update.pluginAnalyzer.PluginAnalyzerService;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class PluginDeploy
{
	private static final Logger LOGGER = Logger.getInstance(PluginDeploy.class);

	@VisibleForTesting
	public static void deployPlugin(UpdateChannel channel, ThrowableComputable<InputStream, IOException> streamSupplier) throws ServiceIsNotReadyException, IOException
	{
		RootService rootService = RootService.getInstance();

		File tempFile = rootService.createTempFile("deploy", "zip");

		try (InputStream inputStream = streamSupplier.compute())
		{
			try (OutputStream output = new FileOutputStream(tempFile))
			{
				ByteStreams.copy(inputStream, output);
			}
		}

		File deployUnzip = rootService.createTempFile("deploy_unzip", "");
		FileUtilRt.createDirectory(deployUnzip);

		ZipUtil.extract(tempFile, deployUnzip, null);

		loadPlugin(rootService, channel, deployUnzip);

		FileUtilRt.delete(tempFile);
		FileUtilRt.delete(deployUnzip);
	}

	private static void loadPlugin(RootService rootService, UpdateChannel channel, File deployUnzip) throws IOException
	{
		List<IdeaPluginDescriptorImpl> pluginDescriptors = new ArrayList<IdeaPluginDescriptorImpl>();
		PluginManagerCore.loadDescriptors(deployUnzip.getAbsolutePath(), pluginDescriptors, null, 1);
		if(pluginDescriptors.size() != 1)
		{
			throw new IllegalArgumentException("Bad plugin [" + pluginDescriptors.size() + "]");
		}

		IdeaPluginDescriptorImpl ideaPluginDescriptor = pluginDescriptors.get(0);

		PluginAnalyzerService pluginAnalyzerService = rootService.getPluginAnalyzerService();

		PluginNode pluginNode = new PluginNode();
		pluginNode.id = ideaPluginDescriptor.getPluginId().getIdString();
		pluginNode.version = stableVersion(ideaPluginDescriptor.getVersion());
		pluginNode.platformVersion = stableVersion(ideaPluginDescriptor.getPlatformVersion());

		pluginNode.name = ideaPluginDescriptor.getName();
		pluginNode.category = ideaPluginDescriptor.getCategory();
		pluginNode.description = ideaPluginDescriptor.getDescription();
		pluginNode.date = System.currentTimeMillis();
		pluginNode.vendor = ideaPluginDescriptor.getVendor();

		pluginNode.optionalDependencies = Arrays.stream(ideaPluginDescriptor.getOptionalDependentPluginIds()).sorted().map(PluginId::getIdString).toArray(String[]::new);

		Set<PluginId> deps = new TreeSet<>();
		Collections.addAll(deps, ideaPluginDescriptor.getDependentPluginIds());

		for(PluginId pluginId : ideaPluginDescriptor.getOptionalDependentPluginIds())
		{
			deps.remove(pluginId);
		}

		pluginNode.dependencies = deps.stream().map(PluginId::getIdString).toArray(String[]::new);

		PluginChannelService pluginChannelService = rootService.getUpdateService(channel);

		try
		{
			PluginNode.Extension[] extensions = new PluginNode.Extension[0];

			MultiMap<String, String> extensionsMap = pluginAnalyzerService.analyze(ideaPluginDescriptor);
			for(Map.Entry<String, Collection<String>> entry : extensionsMap.entrySet())
			{
				PluginNode.Extension extension = new PluginNode.Extension();
				extension.key = entry.getKey();
				extension.values = ArrayUtil.toStringArray(entry.getValue());

				extensions = ArrayUtil.append(extensions, extension);
			}

			pluginNode.extensions = extensions;
		}
		catch(Exception e)
		{
			LOGGER.info(e);
		}

		pluginChannelService.push(pluginNode, f -> {
			try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(f)))
			{
				CommonProcessors.CollectProcessor<File> fileCollectProcessor = new CommonProcessors.CollectProcessor<>();
				File ideaPluginDescriptorPath = ideaPluginDescriptor.getPath();
				assert ideaPluginDescriptorPath != null;
				FileUtil.visitFiles(ideaPluginDescriptorPath, fileCollectProcessor);

				for(File child : fileCollectProcessor.getResults())
				{
					if(child.isDirectory())
					{
						continue;
					}

					String relativePath = FileUtilRt.getRelativePath(ideaPluginDescriptorPath, child);

					ZipEntry zipEntry = new ZipEntry(pluginNode.id + "/" + relativePath);
					zipOutputStream.putNextEntry(zipEntry);

					try (FileInputStream fileOutputStream = new FileInputStream(child))
					{
						ByteStreams.copy(fileOutputStream, zipOutputStream);
					}

					zipOutputStream.closeEntry();
				}
			}
		});
	}

	private static String stableVersion(String value)
	{
		if(StringUtil.isEmpty(value) || "SNAPSHOT".equals(value))
		{
			throw new IllegalArgumentException("Empty or snapshot version is not acceptable");
		}
		return value;
	}
}
