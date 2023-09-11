package consulo.hub.pluginAnalyzer;

import consulo.disposer.Disposable;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.project.event.ProjectManagerListener;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.concurrent.AsyncResult;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 07/05/2023
 */
public class StubProjectManager extends ProjectManager
{
	private final AnalyzerApplication myApplication;
	private AnalyzerProject myDefaultProject;

	public StubProjectManager(AnalyzerApplication analyzerApplication)
	{
		myApplication = analyzerApplication;
	}

	@Nonnull
	@Override
	public Project getDefaultProject()
	{
		if(myDefaultProject == null)
		{
			myDefaultProject = new AnalyzerProject(myApplication);
		}
		return myDefaultProject;
	}

	@Nonnull
	@Override
	public AsyncResult<Project> openProjectAsync(@Nonnull VirtualFile virtualFile, @Nonnull UIAccess uiAccess)
	{
		return AsyncResult.rejected();
	}

	@Nonnull
	@Override
	public AsyncResult<Project> openProjectAsync(@Nonnull Project project, @Nonnull UIAccess uiAccess)
	{
		return AsyncResult.rejected();
	}

	@Override
	public boolean isProjectOpened(Project project)
	{
		return false;
	}

	@Nonnull
	@Override
	public AsyncResult<Void> closeAndDisposeAsync(@Nonnull Project project, @Nonnull UIAccess uiAccess, boolean b, boolean b1, boolean b2)
	{
		return AsyncResult.rejected();
	}

	@Override
	public void addProjectManagerListener(@Nonnull Project project, @Nonnull ProjectManagerListener projectManagerListener)
	{

	}

	@Override
	public void removeProjectManagerListener(@Nonnull Project project, @Nonnull ProjectManagerListener projectManagerListener)
	{

	}

	@Nonnull
	@Override
	public Project[] getOpenProjects()
	{
		return new Project[0];
	}

	@RequiredUIAccess
	@Override
	public boolean closeProject(@Nonnull Project project)
	{
		return false;
	}

	@Override
	public void reloadProject(@Nonnull Project project, @Nonnull UIAccess uiAccess)
	{

	}

	@Nullable
	@Override
	public Project createProject(String s, String s1)
	{
		return null;
	}

	@Override
	public void addProjectManagerListener(@Nonnull ProjectManagerListener projectManagerListener)
	{

	}

	@Override
	public void addProjectManagerListener(@Nonnull ProjectManagerListener projectManagerListener, @Nonnull Disposable disposable)
	{

	}

	@Override
	public void removeProjectManagerListener(@Nonnull ProjectManagerListener projectManagerListener)
	{

	}
}
