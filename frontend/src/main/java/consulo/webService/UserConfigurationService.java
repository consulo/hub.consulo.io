package consulo.webService;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SystemProperties;
import consulo.webService.plugins.PluginAnalyzerService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.util.ConsuloHelper;
import consulo.webService.util.PropertyKeys;
import consulo.webService.util.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@Service
public class UserConfigurationService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginAnalyzerService.class);

	private final PluginChannelService[] myPluginChannelServices;

	private final File myConfigDirectory;

	private File myTempUploadDirectory;

	private AtomicLong myTempCount = new AtomicLong();

	private Executor myExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory()
	{
		private final ThreadGroup myGroup = new ThreadGroup("async delete");

		{
			myGroup.setMaxPriority(Thread.MIN_PRIORITY);
		}

		@Nonnull
		@Override
		public Thread newThread(@Nonnull Runnable r)
		{
			return new Thread(myGroup, r);
		}
	});

	private PropertySet myPropertySet;
	private TaskExecutor myTaskExecutor;

	@Autowired
	public UserConfigurationService(@Nonnull TaskExecutor taskExecutor)
	{
		this(SystemProperties.getUserHome(), taskExecutor);
	}

	@VisibleForTesting
	public UserConfigurationService(String userHome, @Nonnull TaskExecutor taskExecutor)
	{
		myTaskExecutor = taskExecutor;

		ConsuloHelper.init();

		PluginChannel[] values = PluginChannel.values();
		myPluginChannelServices = new PluginChannelService[values.length];
		for(int i = 0; i < values.length; i++)
		{
			myPluginChannelServices[i] = new PluginChannelService(values[i]);
		}

		myConfigDirectory = new File(userHome, ".consuloWebservice");

		FileUtilRt.createDirectory(myConfigDirectory);
	}

	@Nonnull
	public PropertySet getPropertySet()
	{
		return Objects.requireNonNull(myPropertySet);
	}

	public boolean isNotInstalled()
	{
		return myPropertySet == null;
	}

	public void setProperties(Properties properties)
	{
		File file = new File(myConfigDirectory, "config.xml");
		FileSystemUtils.deleteRecursively(file);

		try (FileOutputStream fileOutputStream = new FileOutputStream(file))
		{
			properties.storeToXML(fileOutputStream, "hub.consulo.io");

			reloadProperties();
		}
		catch(IOException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void reloadProperties()
	{
		File file = new File(myConfigDirectory, "config.xml");
		if(file.exists())
		{
			Properties properties = new Properties();
			try
			{
				PropertySet oldPropertySet = myPropertySet;

				try (FileInputStream in = new FileInputStream(file))
				{
					properties.loadFromXML(in);
				}
				myPropertySet = new PropertySet(properties);

				onPropertySetChanged(oldPropertySet, myPropertySet);
			}
			catch(Exception e)
			{
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	@Nonnull
	public PluginChannelService getRepositoryByChannel(@Nonnull PluginChannel channel)
	{
		return myPluginChannelServices[channel.ordinal()];
	}

	@Nonnull
	public File createTempFile(String prefix, @Nullable String ext)
	{
		long l = myTempCount.incrementAndGet();

		File file = new File(myTempUploadDirectory, StringUtil.isEmpty(ext) ? prefix + "_" + l : prefix + "_" + l + "." + ext);
		if(file.exists())
		{
			FileSystemUtils.deleteRecursively(file);
		}

		return file;
	}

	public void asyncDelete(File... files)
	{
		if(files.length == 0)
		{
			return;
		}
		myExecutor.execute(() ->
		{
			for(File file : files)
			{
				FileSystemUtils.deleteRecursively(file);
			}
		});
	}

	@PostConstruct
	public void contextInitialized()
	{
		reloadProperties();
	}

	private void onPropertySetChanged(@Nullable PropertySet oldPropertySet, @Nonnull PropertySet propertySet)
	{
		myTaskExecutor.execute(() ->
		{
			String workDirValue = propertySet.getStringProperty(PropertyKeys.WORKING_DIRECTORY);

			if(oldPropertySet != null)
			{
				String oldWorkDirectory = oldPropertySet.getStringProperty(PropertyKeys.WORKING_DIRECTORY);
				if(Comparing.equal(workDirValue, oldWorkDirectory))
				{
					return;
				}
			}

			if(StringUtil.isEmpty(workDirValue))
			{
				workDirValue = myConfigDirectory.getPath();
			}

			File workingDirectory = new File(workDirValue);
			FileUtilRt.createDirectory(workingDirectory);

			myTempUploadDirectory = new File(workingDirectory, "tempUpload");
			FileSystemUtils.deleteRecursively(myTempUploadDirectory);
			FileUtilRt.createDirectory(myTempUploadDirectory);

			File pluginChannelDir = new File(workingDirectory, "plugin");
			FileUtilRt.createDirectory(pluginChannelDir);

			for(PluginChannelService service : myPluginChannelServices)
			{
				try
				{
					service.initImpl(pluginChannelDir);
				}
				catch(Exception e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		});
	}
}
