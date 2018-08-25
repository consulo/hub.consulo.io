package consulo.webService.plugins;

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

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.google.common.io.ByteStreams;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.ZipUtil;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.archive.TarGzArchive;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
@Service
public class PluginDeployService
{
	private static final Logger logger = LoggerFactory.getLogger(PluginDeployService.class);

	private UserConfigurationService myUserConfigurationService;

	private PluginAnalyzerService myPluginAnalyzerService;

	@Autowired
	public PluginDeployService(UserConfigurationService userConfigurationService, PluginAnalyzerService pluginAnalyzerService)
	{
		myUserConfigurationService = userConfigurationService;
		myPluginAnalyzerService = pluginAnalyzerService;
	}

	@NotNull
	public PluginNode deployPlatform(@NotNull PluginChannel channel, int platformVersion, @NotNull MultipartFile multipartFile) throws Exception
	{
		File tempFile = myUserConfigurationService.createTempFile("deploy", "tar.gz");

		multipartFile.transferTo(tempFile);

		String pluginId = multipartFile.getOriginalFilename().replace(".tar.gz", "");

		PluginNode pluginNode = deployPlatform(channel, platformVersion, pluginId, tempFile);

		myUserConfigurationService.asyncDelete(tempFile);

		return pluginNode;
	}

	@NotNull
	public PluginNode deployPlatform(@NotNull PluginChannel channel, int platformVersion, @NotNull String pluginId, @NotNull File tempFile) throws Exception
	{
		File deployPlatform = myUserConfigurationService.createTempFile("deploy_platform_extract", null);

		TarGzArchive archive = new TarGzArchive();

		archive.extract(tempFile, deployPlatform);

		PluginNode pluginNode = deployPlatformImpl(channel, pluginId, platformVersion, archive, "tar.gz");

		if(pluginId.startsWith("consulo-win"))
		{
			// special hack for windows
			deployPlatformImpl(channel, pluginId + "-zip", platformVersion, archive, "zip");
		}

		myUserConfigurationService.asyncDelete(deployPlatform);

		return pluginNode;
	}

	@NotNull
	private PluginNode deployPlatformImpl(PluginChannel channel, String pluginId, int platformVersion, TarGzArchive archive, String ext) throws Exception
	{
		PluginNode pluginNode = new PluginNode();
		pluginNode.id = pluginId;
		pluginNode.version = String.valueOf(platformVersion);
		pluginNode.name = "Platform";
		pluginNode.platformVersion = String.valueOf(platformVersion);

		// remove old plugin channel markets
		for(PluginChannel pluginChannel : PluginChannel.values())
		{
			archive.removeEntry(makePluginChannelFileName(pluginId, pluginChannel));
		}

		archive.putEntry(makePluginChannelFileName(pluginId, channel), ArrayUtil.EMPTY_BYTE_ARRAY);

		PluginChannelService pluginChannelService = myUserConfigurationService.getRepositoryByChannel(channel);

		String type = ext.equals("zip") ? ArchiveStreamFactory.ZIP : ArchiveStreamFactory.TAR;

		pluginChannelService.push(pluginNode, ext, f -> archive.create(f, type));

		return pluginNode;
	}

	private static String makePluginChannelFileName(String pluginId, PluginChannel pluginChannel)
	{
		boolean mac = pluginId.startsWith("consulo-mac");
		if(mac)
		{
			return "Consulo.app/Contents/." + pluginChannel.name();
		}
		else
		{
			return "Consulo/." + pluginChannel.name();
		}
	}

	public PluginNode deployPlugin(PluginChannel channel, ThrowableComputable<InputStream, IOException> streamSupplier) throws Exception
	{
		File tempFile = myUserConfigurationService.createTempFile("deploy", "zip");

		try (InputStream inputStream = streamSupplier.compute())
		{
			try (OutputStream output = new FileOutputStream(tempFile))
			{
				ByteStreams.copy(inputStream, output);
			}
		}

		File deployUnzip = myUserConfigurationService.createTempFile("deploy_unzip", "");

		FileUtilRt.createDirectory(deployUnzip);

		ZipUtil.extract(tempFile, deployUnzip, null);

		PluginNode pluginNode = loadPlugin(myUserConfigurationService, channel, deployUnzip);

		myUserConfigurationService.asyncDelete(tempFile);
		myUserConfigurationService.asyncDelete(deployUnzip);
		return pluginNode;
	}

	private PluginNode loadPlugin(UserConfigurationService userConfigurationService, PluginChannel channel, File deployUnzip) throws Exception
	{
		List<IdeaPluginDescriptorImpl> pluginDescriptors = new ArrayList<>();
		PluginManagerCore.loadDescriptors(deployUnzip.getAbsolutePath(), pluginDescriptors, null, 1, false);
		if(pluginDescriptors.size() != 1)
		{
			throw new IllegalArgumentException("Bad plugin [" + pluginDescriptors.size() + "]");
		}

		IdeaPluginDescriptorImpl ideaPluginDescriptor = pluginDescriptors.get(0);

		PluginNode pluginNode = new PluginNode();
		pluginNode.id = ideaPluginDescriptor.getPluginId().getIdString();
		pluginNode.version = stableVersion(ideaPluginDescriptor.getVersion());
		pluginNode.platformVersion = stableVersion(ideaPluginDescriptor.getPlatformVersion());

		pluginNode.name = ideaPluginDescriptor.getName();
		pluginNode.category = ideaPluginDescriptor.getCategory();
		pluginNode.description = ideaPluginDescriptor.getDescription();
		pluginNode.vendor = ideaPluginDescriptor.getVendor();

		pluginNode.optionalDependencies = Arrays.stream(ideaPluginDescriptor.getOptionalDependentPluginIds()).sorted().map(PluginId::getIdString).toArray(String[]::new);

		Set<PluginId> deps = new TreeSet<>();
		Collections.addAll(deps, ideaPluginDescriptor.getDependentPluginIds());

		for(PluginId pluginId : ideaPluginDescriptor.getOptionalDependentPluginIds())
		{
			deps.remove(pluginId);
		}

		pluginNode.dependencies = deps.stream().map(PluginId::getIdString).toArray(String[]::new);

		PluginChannelService pluginChannelService = userConfigurationService.getRepositoryByChannel(channel);

		try
		{
			PluginNode.Extension[] extensions = new PluginNode.Extension[0];

			MultiMap<String, String> extensionsMap = myPluginAnalyzerService.analyze(ideaPluginDescriptor, pluginChannelService, pluginNode.dependencies);
			for(Map.Entry<String, Collection<String>> entry : extensionsMap.entrySet())
			{
				PluginNode.Extension extension = new PluginNode.Extension();
				extension.key = entry.getKey();
				extension.values = ArrayUtil.toStringArray(entry.getValue());

				extensions = ArrayUtil.append(extensions, extension);
			}

			pluginNode.extensions = extensions.length == 0 ? null : extensions;
		}
		catch(Exception e)
		{
			logger.info(e.getMessage(), e);
		}

		pluginChannelService.push(pluginNode, "zip", f -> {
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

		return pluginNode;
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
