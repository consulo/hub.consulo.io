package consulo.hub.pluginAnalyzer;

import consulo.annotation.component.ComponentScope;
import consulo.application.Application;
import consulo.component.impl.internal.BaseComponentManager;
import consulo.component.internal.ComponentBinding;
import consulo.component.internal.inject.InjectingContainer;
import consulo.component.internal.inject.InjectingContainerBuilder;
import consulo.project.Project;
import consulo.util.concurrent.coroutine.CoroutineContext;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 07/05/2023
 */
public class AnalyzerProject extends BaseComponentManager implements Project {
    public AnalyzerProject(Application application, ComponentBinding componentBinding) {
        super(application, "AnalyzerProject", ComponentScope.PROJECT, componentBinding, false);

        buildInjectingContainer();
    }

    @Nonnull
    @Override
    protected InjectingContainer findRootContainer() {
        return InjectingContainer.root(getClass().getClassLoader());
    }

    @Override
    protected void bootstrapInjectingContainer(@Nonnull InjectingContainerBuilder builder) {
        super.bootstrapInjectingContainer(builder);

        builder.bind(Project.class).to(this);
    }

    @Nonnull
    @Override
    public Application getApplication() {
        return (Application) getParent();
    }

    @Nonnull
    @Override
    public String getName() {
        return "AnalyzerProject";
    }

    @Nullable
    @Override
    public VirtualFile getBaseDir() {
        return null;
    }

    @Override
    public String getBasePath() {
        return null;
    }

    @Nullable
    @Override
    public VirtualFile getProjectFile() {
        return null;
    }

    @Nonnull
    @Override
    public String getProjectFilePath() {
        return null;
    }

    @Nullable
    @Override
    public String getPresentableUrl() {
        return null;
    }

    @Nullable
    @Override
    public VirtualFile getWorkspaceFile() {
        return null;
    }

    @Nonnull
    @Override
    public String getLocationHash() {
        return null;
    }

    @Override
    public void save() {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Nonnull
    @Override
    public CoroutineContext coroutineContext() {
        throw new UnsupportedOperationException();
    }
}
