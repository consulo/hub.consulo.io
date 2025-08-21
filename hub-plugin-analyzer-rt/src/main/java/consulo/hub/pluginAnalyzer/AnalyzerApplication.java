package consulo.hub.pluginAnalyzer;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ComponentScope;
import consulo.application.AccessToken;
import consulo.application.Application;
import consulo.application.event.ApplicationListener;
import consulo.component.ComponentManager;
import consulo.component.bind.InjectingBinding;
import consulo.component.impl.internal.BaseComponentManager;
import consulo.component.internal.ComponentBinding;
import consulo.component.internal.inject.InjectingContainer;
import consulo.component.internal.inject.InjectingContainerBuilder;
import consulo.disposer.Disposable;
import consulo.project.ProjectManager;
import consulo.ui.ModalityState;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.util.collection.MultiMap;
import consulo.util.lang.function.ThrowableSupplier;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import jakarta.annotation.Nonnull;

import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class AnalyzerApplication extends BaseComponentManager implements Application {
    private final ComponentBinding myComponentBinding;
    private final Disposable myLastDisposable;

    public AnalyzerApplication(Disposable lastDisposable, ComponentBinding componentBinding) {
        super(null, "AnalyzerApplication", ComponentScope.APPLICATION, componentBinding, false);
        myLastDisposable = lastDisposable;
        myComponentBinding = componentBinding;

        buildInjectingContainer();
    }

    @Override
    public ComponentManager getApplication() {
        return this;
    }

    @Override
    protected void fillListenerDescriptors(MultiMap<String, InjectingBinding> mapByTopic) {
    }

    @Nonnull
    @Override
    protected InjectingContainer findRootContainer() {
        return InjectingContainer.root(getClass().getClassLoader());
    }

    @Override
    protected void bootstrapInjectingContainer(@Nonnull InjectingContainerBuilder builder) {
        super.bootstrapInjectingContainer(builder);

        builder.bind(Application.class).to(this);
        // this fix for ArchiveFileType which require ArchiveFileType VirtualFileManager inside contructor
        builder.bind(VirtualFileManager.class).to(new StubVirtualFileManager());
        builder.bind(FileNameMatcherFactory.class).to(new FileNameMatcherFactoryImpl());
        builder.bind(ProjectManager.class).to(new StubProjectManager(this, myComponentBinding));
    }

    @Override
    public void runReadAction(@Nonnull Runnable action) {
        action.run();
    }

    @Override
    public <T> T runReadAction(@Nonnull Supplier<T> computation) {
        return computation.get();
    }

    @Override
    public boolean tryRunReadAction(@Nonnull Runnable action) {
        action.run();
        return true;
    }

    @RequiredUIAccess
    @Override
    public void runWriteAction(@Nonnull Runnable action) {
        throw new UnsupportedOperationException();
    }

    @RequiredUIAccess
    @Override
    public <T> T runWriteAction(@Nonnull Supplier<T> computation) {
        throw new UnsupportedOperationException();
    }

    @RequiredReadAction
    @Override
    public void assertReadAccessAllowed() {

    }

    @RequiredWriteAction
    @Override
    public void assertWriteAccessAllowed() {

    }

    @RequiredUIAccess
    @Override
    public void assertIsDispatchThread() {

    }

    @Override
    public void addApplicationListener(@Nonnull ApplicationListener listener) {

    }

    @Override
    public void addApplicationListener(@Nonnull ApplicationListener listener, @Nonnull Disposable parent) {

    }

    @Override
    public void removeApplicationListener(@Nonnull ApplicationListener listener) {

    }

    @RequiredUIAccess
    @Override
    public void saveAll() {

    }

    @Override
    public void saveSettings() {

    }

    @Override
    public void exit() {

    }

    @Override
    public boolean isReadAccessAllowed() {
        return true;
    }

    @Override
    public boolean isDispatchThread() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWriteThread() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invokeLater(@Nonnull Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invokeLater(@Nonnull Runnable runnable, @Nonnull BooleanSupplier expired) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invokeLater(@Nonnull Runnable runnable, @Nonnull consulo.ui.ModalityState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invokeLater(@Nonnull Runnable runnable, @Nonnull consulo.ui.ModalityState state, @Nonnull BooleanSupplier expired) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invokeAndWait(@Nonnull Runnable runnable, @Nonnull consulo.ui.ModalityState modalityState) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ModalityState getCurrentModalityState() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ModalityState getModalityStateForComponent(@Nonnull Component c) {
        return getNoneModalityState();
    }

    @Nonnull
    @Override
    public ModalityState getDefaultModalityState() {
        return getNoneModalityState();
    }

    @Nonnull
    @Override
    public ModalityState getNoneModalityState() {
        return ModalityState.nonModal();
    }

    @Nonnull
    @Override
    public ModalityState getAnyModalityState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @RequiredUIAccess
    @Override
    public long getIdleTime() {
        return 0;
    }

    @Override
    public boolean isHeadlessEnvironment() {
        return true;
    }

    @Nonnull
    @Override
    public Future<?> executeOnPooledThread(@Nonnull Runnable action) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public <T> Future<T> executeOnPooledThread(@Nonnull Callable<T> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDisposeInProgress() {
        return false;
    }

    @Override
    public boolean isRestartCapable() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Nonnull
    @Override
    public Image getIcon() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public UIAccess getLastUIAccess() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public AccessToken acquireReadActionLock() {
        throw new UnsupportedOperationException();
    }

    @RequiredUIAccess
    @Nonnull
    @Override
    public AccessToken acquireWriteActionLock(@Nonnull Class marker) {
        return AccessToken.EMPTY_ACCESS_TOKEN;
    }

    @RequiredUIAccess
    @Override
    public <T, E extends Throwable> T runWriteAction(@Nonnull ThrowableSupplier<T, E> computation) throws E {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasWriteAction(@Nonnull Class<?> actionClass) {
        return false;
    }

    @Override
    public <T, E extends Throwable> T runReadAction(@Nonnull ThrowableSupplier<T, E> computation) throws E {
        return computation.get();
    }
}
