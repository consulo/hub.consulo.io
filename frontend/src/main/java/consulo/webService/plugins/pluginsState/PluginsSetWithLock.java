package consulo.webService.plugins.pluginsState;

import java.io.File;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.util.io.FileUtilRt;
import consulo.webService.plugins.PluginNode;

/**
 * @author VISTALL
 * @since 09-May-17
 */
public class PluginsSetWithLock extends PluginsState
{
	private static class LockWrapper extends AccessToken
	{
		private final Lock myLock;

		private LockWrapper(Lock lock)
		{
			myLock = lock;
			myLock.lock();
		}

		@Override
		public void finish()
		{
			myLock.unlock();
		}
	}

	private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();

	public PluginsSetWithLock(File rootDir, String pluginId)
	{
		super(rootDir, pluginId);

		FileUtilRt.createParentDirs(myPluginDirectory);
	}

	@NotNull
	@Override
	public NavigableMap<String, NavigableSet<PluginNode>> getPluginsByPlatformVersion()
	{
		throw new UnsupportedOperationException("Plugins state with locks, does not support this method. Use copy() method before");
	}

	@Override
	protected AccessToken readLock()
	{
		return new LockWrapper(myLock.readLock());
	}

	@Override
	protected AccessToken writeLock()
	{
		return new LockWrapper(myLock.writeLock());
	}
}
