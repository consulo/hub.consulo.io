package consulo.hub.backend.repository;

import com.google.common.io.ByteStreams;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.MultiMap;
import consulo.container.impl.ContainerLogger;
import consulo.container.impl.PluginDescriptorImpl;
import consulo.container.impl.PluginDescriptorLoader;
import consulo.container.plugin.PluginId;
import consulo.hub.backend.repository.archive.TarGzArchive;
import consulo.hub.backend.util.ZipUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
@Service
public class PluginDeployService
{
	private static class OwnContainerLogger implements ContainerLogger
	{
		private static final OwnContainerLogger ourInstance = new OwnContainerLogger();

		@Override
		public void info(String message)
		{
			logger.info(message);
		}

		@Override
		public void warn(String message)
		{
			logger.warn(message);
		}

		@Override
		public void info(String message, Throwable throwable)
		{
			logger.info(message, throwable);
		}

		@Override
		public void error(String message, Throwable throwable)
		{
			logger.error(message, throwable);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(PluginDeployService.class);

	private PluginChannelsService myUserConfigurationService;

	private PluginAnalyzerService myPluginAnalyzerService;

	@Autowired
	public PluginDeployService(PluginChannelsService userConfigurationService, PluginAnalyzerService pluginAnalyzerService)
	{
		myUserConfigurationService = userConfigurationService;
		myPluginAnalyzerService = pluginAnalyzerService;
	}

	@Nonnull
	public PluginNode deployPlatform(@Nonnull PluginChannel channel, int platformVersion, @Nonnull MultipartFile multipartFile) throws Exception
	{
		File tempFile = myUserConfigurationService.createTempFile("deploy", "tar.gz");

		multipartFile.transferTo(tempFile);

		String pluginId = multipartFile.getOriginalFilename().replace(".tar.gz", "");

		PluginNode pluginNode = deployPlatform(channel, platformVersion, pluginId, tempFile);

		myUserConfigurationService.asyncDelete(tempFile);

		return pluginNode;
	}

	@Nonnull
	public PluginNode deployPlatform(@Nonnull PluginChannel channel, int platformVersion, @Nonnull String pluginId, @Nonnull File tempFile) throws Exception
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

	@Nonnull
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

		archive.putEntry(makePluginChannelFileName(pluginId, channel), ArrayUtil.EMPTY_BYTE_ARRAY, System.currentTimeMillis());

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

		try(ZipFile zipFile = new ZipFile(tempFile))
		{
			ZipUtil.extract(zipFile, deployUnzip);
		}

		PluginNode pluginNode = loadPlugin(myUserConfigurationService, channel, deployUnzip);

		myUserConfigurationService.asyncDelete(tempFile);
		myUserConfigurationService.asyncDelete(deployUnzip);
		return pluginNode;
	}

	private PluginNode loadPlugin(PluginChannelsService userConfigurationService, PluginChannel channel, File deployUnzip) throws Exception
	{
		List<PluginDescriptorImpl> pluginDescriptors = new ArrayList<>();

		loadDescriptors(deployUnzip, pluginDescriptors);

		if(pluginDescriptors.size() != 1)
		{
			throw new IllegalArgumentException("Bad plugin [" + pluginDescriptors.size() + "]");
		}

		PluginDescriptorImpl pluginDescriptor = pluginDescriptors.get(0);

		PluginNode pluginNode = new PluginNode();
		pluginNode.id = pluginDescriptor.getPluginId().getIdString();
		pluginNode.version = stableVersion(pluginDescriptor.getVersion());
		pluginNode.platformVersion = stableVersion(pluginDescriptor.getPlatformVersion());

		pluginNode.name = pluginDescriptor.getName();
		pluginNode.category = pluginDescriptor.getCategory();
		pluginNode.description = pluginDescriptor.getDescription();
		pluginNode.vendor = pluginDescriptor.getVendor();
		pluginNode.experimental = pluginDescriptor.isExperimental();
		byte[] iconBytes = pluginDescriptor.getIconBytes();
		pluginNode.iconBytes = iconBytes.length == 0 ? null : Base64.getEncoder().encodeToString(iconBytes);

		pluginNode.optionalDependencies = Arrays.stream(pluginDescriptor.getOptionalDependentPluginIds()).sorted().map(PluginId::getIdString).toArray(String[]::new);

		Set<PluginId> deps = new TreeSet<>();
		Collections.addAll(deps, pluginDescriptor.getDependentPluginIds());

		for(PluginId pluginId : pluginDescriptor.getOptionalDependentPluginIds())
		{
			deps.remove(pluginId);
		}

		pluginNode.dependencies = deps.stream().map(PluginId::getIdString).toArray(String[]::new);
		pluginNode.incompatibleWiths = Arrays.stream(pluginDescriptor.getIncompatibleWithPlugindIds()).map(PluginId::getIdString).toArray(String[]::new);

		PluginChannelService pluginChannelService = userConfigurationService.getRepositoryByChannel(channel);

		try
		{
			PluginAnalyzerService.ExtensionsResult result = myPluginAnalyzerService.analyze(pluginDescriptor, pluginChannelService, pluginNode.dependencies);

			pluginNode.extensions = convert(result.v1);
			pluginNode.extensionsV2 = convert(result.v2);
		}
		catch(Exception e)
		{
			logger.info(e.getMessage(), e);
		}

		pluginChannelService.push(pluginNode, "zip", f -> {
			try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(f)))
			{
				CommonProcessors.CollectProcessor<File> fileCollectProcessor = new CommonProcessors.CollectProcessor<>();
				File ideaPluginDescriptorPath = pluginDescriptor.getPath();
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
					//BasicFileAttributes attr = Files.readAttributes(child.toPath(), BasicFileAttributes.class);

					zipEntry.setTime(child.lastModified());
					//zipEntry.setCreationTime(attr.creationTime());
					//zipEntry.setLastAccessTime(attr.lastAccessTime());
					//zipEntry.setLastModifiedTime(attr.lastModifiedTime());

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

	public static void loadDescriptors(@Nonnull File pluginsHome, @Nonnull List<PluginDescriptorImpl> result)
	{
		final File[] files = pluginsHome.listFiles();
		if(files != null)
		{
			for(File file : files)
			{
				final PluginDescriptorImpl descriptor = PluginDescriptorLoader.loadDescriptor(file, true, false, OwnContainerLogger.ourInstance);
				if(descriptor == null)
				{
					continue;
				}

				result.add(descriptor);
			}
		}
	}

	@Nullable
	private static PluginNode.Extension[] convert(MultiMap<String, String> extensions)
	{
		PluginNode.Extension[] extensionsV1 = new PluginNode.Extension[0];

		for(Map.Entry<String, Collection<String>> entry : extensions.entrySet())
		{
			PluginNode.Extension extension = new PluginNode.Extension();
			extension.key = entry.getKey();
			extension.values = ArrayUtil.toStringArray(entry.getValue());

			extensionsV1 = ArrayUtil.append(extensionsV1, extension);
		}

		return extensionsV1.length == 0 ? null : extensionsV1;
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
