package consulo.hub.backend.repository.impl.store.old;

import consulo.hub.backend.util.AccessToken;
import consulo.hub.shared.repository.PluginNode;
import consulo.util.io.FileUtil;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

		FileUtil.createParentDirs(myPluginDirectory);
	}

	@Nonnull
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
