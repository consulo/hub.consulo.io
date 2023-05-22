package consulo.hub.backend.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import consulo.application.util.function.CommonProcessors;
import consulo.container.impl.ContainerLogger;
import consulo.container.impl.PluginDescriptorImpl;
import consulo.container.impl.PluginDescriptorLoader;
import consulo.container.plugin.PluginId;
import consulo.container.plugin.PluginPermissionDescriptor;
import consulo.container.plugin.PluginPermissionType;
import consulo.hub.backend.TempFileService;
import consulo.hub.backend.github.release.GithubRelease;
import consulo.hub.backend.github.release.GithubReleaseService;
import consulo.hub.backend.github.release.GithubTagBuilder;
import consulo.hub.backend.github.release.RepoGithubTagBuilder;
import consulo.hub.backend.util.ZipUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.io.FileUtil;
import consulo.util.io.UnsyncByteArrayInputStream;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.ThrowableSupplier;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

	public static final int LAST_V2_BUILD = 3115;

	private static final Logger logger = LoggerFactory.getLogger(PluginDeployService.class);

	private final TempFileService myTempFileService;

	private final PluginAnalyzerService myPluginAnalyzerService;

	private final ObjectMapper myObjectMapper;

	private final PluginHistoryService myPluginHistoryService;

	private final RepositoryChannelsService myRepositoryChannelsService;

	private final GithubReleaseService myGithubReleaseService;

	@Autowired
	public PluginDeployService(TempFileService tempFileService,
							   PluginAnalyzerService pluginAnalyzerService,
							   ObjectMapper objectMapper,
							   PluginHistoryService pluginHistoryService,
							   RepositoryChannelsService repositoryChannelsService,
							   GithubReleaseService githubReleaseService)
	{
		myTempFileService = tempFileService;
		myPluginAnalyzerService = pluginAnalyzerService;
		myObjectMapper = objectMapper;
		myPluginHistoryService = pluginHistoryService;
		myRepositoryChannelsService = repositoryChannelsService;
		myGithubReleaseService = githubReleaseService;
	}

	@Nonnull
	public PluginNode deployPlatform(@Nonnull PluginChannel channel, int platformVersion, @Nonnull MultipartFile platformFile, @Nullable MultipartFile history) throws Exception
	{
		Path deployFile = myTempFileService.createTempFilePath(platformFile.getName(), null);

		try (OutputStream outputStream = Files.newOutputStream(deployFile))
		{
			IOUtils.copy(platformFile.getInputStream(), outputStream);
		}

		// cut extension, can be zip/tar.gz/exe
		String nodeId = FileUtil.getNameWithoutExtension(platformFile.getOriginalFilename());
		if(!RepositoryUtil.isPlatformNode(nodeId))
		{
			throw new IllegalArgumentException("Unknown ID: " + nodeId);
		}

		RestPluginHistoryEntry[] historyEntries = processPluginHistory(() -> history == null ? null : history.getInputStream());

		PluginNode pluginNode = deployPlatform(channel, platformVersion, nodeId, deployFile);

		if(historyEntries != null)
		{
			myPluginHistoryService.insert(historyEntries, pluginNode);
		}

		myTempFileService.asyncDelete(deployFile);

		return pluginNode;
	}

	@Nonnull
	public PluginNode deployPlatform(@Nonnull PluginChannel channel, int platformVersion, @Nonnull String pluginId, @Nonnull Path deployFilePath) throws Exception
	{
		return deployPlatformImpl(channel, pluginId, platformVersion, deployFilePath);
	}

	@Nonnull
	private PluginNode deployPlatformImpl(PluginChannel channel, String pluginId, int platformVersion, Path deployPath) throws Exception
	{
		PluginNode pluginNode = new PluginNode();
		pluginNode.id = pluginId;
		pluginNode.version = String.valueOf(platformVersion);
		pluginNode.name = "Platform";
		pluginNode.platformVersion = String.valueOf(platformVersion);

		RepositoryChannelStore repositoryChannelStore = myRepositoryChannelsService.getRepositoryByChannel(channel);

		String ext = myRepositoryChannelsService.getNodeExtension(pluginNode);

		repositoryChannelStore.push(pluginNode, ext, target -> Files.copy(deployPath, target));

		return pluginNode;
	}

	public PluginNode deployPlugin(PluginChannel channel, ThrowableSupplier<InputStream, IOException> streamSupplier) throws Exception
	{
		return deployPlugin(channel, () -> null, streamSupplier, null);
	}

	public PluginNode deployPlugin(PluginChannel channel,
								   ThrowableSupplier<InputStream, IOException> historyStreamSupplier,
								   ThrowableSupplier<InputStream, IOException> streamSupplier,
								   @Nullable RestPluginGithubInfo githubInfo) throws Exception
	{
		Path tempFile = myTempFileService.createTempFilePath("deploy", "zip");

		try (InputStream inputStream = streamSupplier.get())
		{
			try (OutputStream output = Files.newOutputStream(tempFile))
			{
				ByteStreams.copy(inputStream, output);
			}
		}

		Path deployUnzip = myTempFileService.createTempFilePath("deploy_unzip", "");

		Files.createDirectories(deployUnzip);

		try (ZipFile zipFile = new ZipFile(tempFile.toFile()))
		{
			ZipUtil.extract(zipFile, deployUnzip.toFile());
		}

		RestPluginHistoryEntry[] historyEntries = processPluginHistory(historyStreamSupplier);

		PluginNode pluginNode = loadPlugin(channel, deployUnzip);

		if(historyEntries != null)
		{
			myPluginHistoryService.insert(historyEntries, pluginNode);
		}

		if(githubInfo != null)
		{
			GithubTagBuilder builder = new RepoGithubTagBuilder(pluginNode);

			GithubRelease release = myGithubReleaseService.createTagAndRelease(githubInfo.repoUrl, githubInfo.commitSha1, builder);

			try (InputStream inputStream = Files.newInputStream(pluginNode.targetPath))
			{
				String assetUrl = release.uploadAsset(pluginNode.targetPath.getFileName().toString(), "application/zip", inputStream);
				if(assetUrl != null)
				{
					myRepositoryChannelsService.getRepositoryByChannel(channel).attachDownloadUrl(pluginNode, assetUrl);
				}
			}
		}

		myTempFileService.asyncDelete(tempFile);
		myTempFileService.asyncDelete(deployUnzip);
		return pluginNode;
	}

	@Nullable
	private RestPluginHistoryEntry[] processPluginHistory(@Nonnull ThrowableSupplier<InputStream, IOException> historyStreamSupplier) throws IOException
	{
		RestPluginHistoryEntry[] historyEntries = null;
		InputStream historyJsonStream = historyStreamSupplier.get();
		if(historyJsonStream != null)
		{
			try
			{
				historyEntries = myObjectMapper.readValue(historyJsonStream, RestPluginHistoryEntry[].class);
				for(RestPluginHistoryEntry historyEntry : historyEntries)
				{
					if(historyEntry == null || historyEntry.isEmpty())
					{
						throw new IllegalArgumentException("History can't not be empty");
					}
				}

				if(historyEntries.length > 1024)
				{
					throw new IllegalArgumentException("Too big history");
				}
			}
			finally
			{
				IOUtils.close(historyJsonStream);
			}
		}

		return historyEntries;
	}

	private PluginNode loadPlugin(PluginChannel channel, Path deployUnzip) throws Exception
	{
		List<PluginDescriptorImpl> pluginDescriptors = new ArrayList<>();

		loadDescriptors(deployUnzip.toFile(), pluginDescriptors);

		if(pluginDescriptors.size() != 1)
		{
			throw new IllegalArgumentException("Bad plugin [" + pluginDescriptors.size() + "]");
		}

		PluginDescriptorImpl pluginDescriptor = pluginDescriptors.get(0);

		if(pluginDescriptor.getTags().isEmpty())
		{
			throw new IllegalArgumentException("Tags cannot be empty");
		}

		PluginNode pluginNode = new PluginNode();
		pluginNode.id = pluginDescriptor.getPluginId().getIdString();
		pluginNode.version = stableVersion(pluginDescriptor.getVersion());
		pluginNode.platformVersion = stableVersion(pluginDescriptor.getPlatformVersion());

		int platformVersion = Integer.parseInt(stableVersion(pluginDescriptor.getPlatformVersion()));
		if(platformVersion <= LAST_V2_BUILD)
		{
			throw new IOException("Impossible deploy plugins for V2 Consulo");
		}

		pluginNode.name = pluginDescriptor.getName();
		pluginNode.category = pluginDescriptor.getCategory();
		pluginNode.url = pluginDescriptor.getUrl();
		pluginNode.description = pluginDescriptor.getDescription();
		pluginNode.vendor = pluginDescriptor.getVendor();
		pluginNode.vendorUrl = pluginDescriptor.getVendorUrl();
		pluginNode.vendorEmail = pluginDescriptor.getVendorEmail();
		pluginNode.experimental = pluginDescriptor.isExperimental();
		byte[] lightIconBytes = pluginDescriptor.getIconBytes(false);
		byte[] darkIconBytes = pluginDescriptor.getIconBytes(true);
		pluginNode.iconBytes = prepareSVG(lightIconBytes);
		if(lightIconBytes != darkIconBytes)
		{
			pluginNode.iconDarkBytes = prepareSVG(darkIconBytes);
		}

		pluginNode.optionalDependencies = Arrays.stream(pluginDescriptor.getOptionalDependentPluginIds()).sorted().map(PluginId::getIdString).toArray(String[]::new);

		List<PluginNode.Permission> permissions = new ArrayList<>();
		for(PluginPermissionType type : PluginPermissionType.values())
		{
			PluginPermissionDescriptor descriptor = pluginDescriptor.getPermissionDescriptor(type);
			if(descriptor != null)
			{
				PluginNode.Permission permission = new PluginNode.Permission();
				permission.type = type.name();

				Set<String> options = descriptor.getOptions();
				permission.options = options.isEmpty() ? null : options.toArray(String[]::new);

				permissions.add(permission);
			}
		}

		pluginNode.permissions = permissions.toArray(PluginNode.Permission[]::new);

		pluginNode.tags = pluginDescriptor.getTags().toArray(String[]::new);

		Set<PluginId> deps = new TreeSet<>();
		Collections.addAll(deps, pluginDescriptor.getDependentPluginIds());

		for(PluginId pluginId : pluginDescriptor.getOptionalDependentPluginIds())
		{
			deps.remove(pluginId);
		}

		pluginNode.dependencies = deps.stream().map(PluginId::getIdString).toArray(String[]::new);
		pluginNode.incompatibleWiths = Arrays.stream(pluginDescriptor.getIncompatibleWithPlugindIds()).map(PluginId::getIdString).toArray(String[]::new);

		RepositoryChannelStore repositoryChannelStore = myRepositoryChannelsService.getRepositoryByChannel(channel);

		try
		{
			pluginNode.extensionPreviews = myPluginAnalyzerService.analyze(deployUnzip.toFile(), pluginDescriptor, repositoryChannelStore);
		}
		catch(Throwable e)
		{
			logger.info(e.getMessage(), e);
		}

		repositoryChannelStore.push(pluginNode, myRepositoryChannelsService.getDeployPluginExtension(), path ->
		{
			try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(path)))
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

					String relativePath = FileUtil.getRelativePath(ideaPluginDescriptorPath, child);

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

	private static String prepareSVG(@Nullable byte[] iconBytes) throws Exception
	{
		if(iconBytes == null || iconBytes.length == 0)
		{
			return null;
		}

		try
		{
			SAXBuilder saxBuilder = new SAXBuilder();
			saxBuilder.setValidation(false);

			Document document = saxBuilder.build(new UnsyncByteArrayInputStream(iconBytes));

			removeComments(document.getRootElement());

			ByteArrayOutputStream out = new ByteArrayOutputStream();

			XMLOutputter writer = new XMLOutputter(Format.getCompactFormat());
			writer.output(document, out);

			out.close();

			byte[] bytes = out.toByteArray();
			return Base64.getEncoder().encodeToString(bytes);
		}
		catch(Throwable e)
		{
			throw new IllegalArgumentException("Failed to analyze icon", e);
		}
	}

	private static void removeComments(Element element)
	{
		List<Content> toRemove = new ArrayList<>();
		for(Content content : element.getContent())
		{
			if(content instanceof Comment)
			{
				toRemove.add(content);
			}
			else if(content instanceof Element)
			{
				removeComments((Element) content);
			}
		}

		for(Content content : toRemove)
		{
			element.removeContent(content);
		}
	}

	public static void loadDescriptors(@Nonnull File pluginsHome, @Nonnull List<PluginDescriptorImpl> result)
	{
		final File[] files = pluginsHome.listFiles();
		if(files != null)
		{
			for(File file : files)
			{
				final PluginDescriptorImpl descriptor = PluginDescriptorLoader.loadDescriptor(file, false, OwnContainerLogger.ourInstance);
				if(descriptor == null)
				{
					continue;
				}

				result.add(descriptor);
			}
		}
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
