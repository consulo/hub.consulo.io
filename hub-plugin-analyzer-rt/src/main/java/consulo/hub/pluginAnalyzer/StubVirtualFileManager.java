package consulo.hub.pluginAnalyzer;

import consulo.component.ComponentManager;
import consulo.component.util.Iconable;
import consulo.disposer.Disposable;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.VirtualFileSystem;
import consulo.virtualFileSystem.event.AsyncFileListener;
import consulo.virtualFileSystem.event.VirtualFileListener;
import consulo.virtualFileSystem.event.VirtualFileManagerListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class StubVirtualFileManager extends VirtualFileManager
{
	@Override
	public VirtualFileSystem getFileSystem(@Nonnull String s)
	{
		return null;
	}

	@Override
	public long syncRefresh()
	{
		return 0;
	}

	@Override
	public long asyncRefresh(@Nullable Runnable runnable)
	{
		return 0;
	}

	@Override
	public void refreshWithoutFileWatcher(boolean b)
	{

	}

	@Nullable
	@Override
	public VirtualFile findFileByUrl(@Nonnull String s)
	{
		return null;
	}

	@Nullable
	@Override
	public VirtualFile refreshAndFindFileByUrl(@Nonnull String s)
	{
		return null;
	}

	@Override
	public void addVirtualFileListener(@Nonnull VirtualFileListener virtualFileListener)
	{

	}

	@Override
	public void addVirtualFileListener(@Nonnull VirtualFileListener virtualFileListener, @Nonnull Disposable disposable)
	{

	}

	@Override
	public void removeVirtualFileListener(@Nonnull VirtualFileListener virtualFileListener)
	{

	}

	@Override
	public void addVirtualFileManagerListener(@Nonnull VirtualFileManagerListener virtualFileManagerListener)
	{

	}

	@Override
	public void addVirtualFileManagerListener(@Nonnull VirtualFileManagerListener virtualFileManagerListener, @Nonnull Disposable disposable)
	{

	}

	@Override
	public void removeVirtualFileManagerListener(@Nonnull VirtualFileManagerListener virtualFileManagerListener)
	{

	}

	@Override
	public void addAsyncFileListener(@Nonnull AsyncFileListener asyncFileListener, @Nonnull Disposable disposable)
	{

	}

	@Override
	public void notifyPropertyChanged(@Nonnull VirtualFile virtualFile, @Nonnull String s, Object o, Object o1)
	{

	}

	@Override
	public long getModificationCount()
	{
		return 0;
	}

	@Override
	public long getStructureModificationCount()
	{
		return 0;
	}

	@Override
	public int storeName(@Nonnull String s)
	{
		return 0;
	}

	@Nonnull
	@Override
	public CharSequence getVFileName(int i)
	{
		return null;
	}

	@Override
	public Image getBaseFileIcon(@Nonnull VirtualFile virtualFile)
	{
		return null;
	}

	@Override
	public Image getFileIcon(@Nonnull VirtualFile virtualFile, @Nullable ComponentManager componentManager, @Iconable.IconFlags int i)
	{
		return null;
	}
}
