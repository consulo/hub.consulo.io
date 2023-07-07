package consulo.hub.backend.repository.impl.store.neww;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import consulo.hub.backend.TempFileService;
import consulo.hub.backend.WorkDirectoryService;
import consulo.hub.backend.repository.RepositoryChannelStore;
import consulo.hub.backend.repository.RepositoryChannelsService;
import consulo.hub.shared.repository.PluginChannel;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class NewRepositoryChannelsService implements RepositoryChannelsService
{
	private final NewRepositoryChannelStore[] myPluginChannelServices;
	private final WorkDirectoryService myWorkDirectoryService;
	private final TaskExecutor myTaskExecutor;

	private final NewInlineRepositoryStore myInlineRepositoryStore;

	public NewRepositoryChannelsService(WorkDirectoryService workDirectoryService, TempFileService tempFileService, TaskExecutor taskExecutor)
	{
		myWorkDirectoryService = workDirectoryService;
		myTaskExecutor = taskExecutor;

		myInlineRepositoryStore = new NewInlineRepositoryStore(workDirectoryService, tempFileService);

		PluginChannel[] values = PluginChannel.values();
		myPluginChannelServices = new NewRepositoryChannelStore[values.length];
		for(int i = 0; i < values.length; i++)
		{
			myPluginChannelServices[i] = new NewRepositoryChannelStore(values[i], myInlineRepositoryStore, this);
		}
	}

	public NewInlineRepositoryStore getInlineRepositoryStore()
	{
		return myInlineRepositoryStore;
	}

	@Nonnull
	@Override
	public RepositoryChannelStore getRepositoryByChannel(@Nonnull PluginChannel channel)
	{
		return myPluginChannelServices[channel.ordinal()];
	}

	@PostConstruct
	public void init() throws Exception
	{
		boolean runImport = myInlineRepositoryStore.init();

		Path tempDir = myWorkDirectoryService.getWorkingDirectory().resolve("tempDir");
		if(Files.exists(tempDir))
		{
			FileSystemUtils.deleteRecursively(tempDir);
		}

		Files.createDirectory(tempDir);

		myTaskExecutor.execute(() ->
		{
			if(runImport)
			{
				myInlineRepositoryStore.runImport(this);
			}

			myInlineRepositoryStore.load(this);
		});
	}
}
