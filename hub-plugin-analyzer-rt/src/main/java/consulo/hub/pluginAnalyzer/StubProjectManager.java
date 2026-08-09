package consulo.hub.pluginAnalyzer;

import consulo.component.internal.ComponentBinding;
import consulo.disposer.Disposable;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.project.ProjectOpenContext;
import consulo.project.event.ProjectManagerListener;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.concurrent.AsyncResult;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * @author VISTALL
 * @since 07/05/2023
 */
public class StubProjectManager implements ProjectManager {
    private final AnalyzerApplication myApplication;
    private final ComponentBinding myComponentBinding;
    private AnalyzerProject myDefaultProject;

    public StubProjectManager(AnalyzerApplication analyzerApplication, ComponentBinding componentBinding) {
        myApplication = analyzerApplication;
        myComponentBinding = componentBinding;
    }

    @Nonnull
    @Override
    public Project getDefaultProject() {
        if (myDefaultProject == null) {
            myDefaultProject = new AnalyzerProject(myApplication, myComponentBinding);
        }
        return myDefaultProject;
    }

    @Override
    public boolean isProjectOpened(Project project) {
        return false;
    }

    @Override
    public CompletableFuture<Boolean> closeAndDisposeAsync(Project project, UIAccess uiAccess, boolean b, boolean b1, boolean b2) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Project> openProjectAsync(Path path, UIAccess uiAccess, ProjectOpenContext projectOpenContext) {
        return CompletableFuture.failedFuture(new IllegalArgumentException());
    }

    @Override
    public CompletableFuture<Boolean> closeAndDisposeAsync(Project project, UIAccess uiAccess) {
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public void addProjectManagerListener(@Nonnull Project project, @Nonnull ProjectManagerListener projectManagerListener) {

    }

    @Override
    public void removeProjectManagerListener(@Nonnull Project project, @Nonnull ProjectManagerListener projectManagerListener) {

    }

    @Nonnull
    @Override
    public Project[] getOpenProjects() {
        return new Project[0];
    }

    @Override
    public void reloadProject(@Nonnull Project project, @Nonnull UIAccess uiAccess) {

    }

    @Nullable
    @Override
    public Project createProject(String s, String s1) {
        return null;
    }

    @Override
    public void addProjectManagerListener(@Nonnull ProjectManagerListener projectManagerListener) {

    }

    @Override
    public void addProjectManagerListener(@Nonnull ProjectManagerListener projectManagerListener, @Nonnull Disposable disposable) {

    }

    @Override
    public void removeProjectManagerListener(@Nonnull ProjectManagerListener projectManagerListener) {

    }
}
