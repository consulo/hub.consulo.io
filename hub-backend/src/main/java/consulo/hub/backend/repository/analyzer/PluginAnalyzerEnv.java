package consulo.hub.backend.repository.analyzer;

import com.google.gson.Gson;
import consulo.annotation.component.ComponentScope;
import consulo.application.Application;
import consulo.application.impl.internal.plugin.PluginsLoader;
import consulo.compiler.Compiler;
import consulo.compiler.artifact.ArtifactType;
import consulo.component.ComponentManager;
import consulo.component.impl.internal.BaseComponentManager;
import consulo.component.store.impl.internal.IComponentStore;
import consulo.container.impl.ContainerLogger;
import consulo.container.plugin.PluginDescriptor;
import consulo.content.base.BinariesOrderRootType;
import consulo.disposer.Disposer;
import consulo.disposer.internal.impl.DisposerInternalImpl;
import consulo.document.FileDocumentManager;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.coverage.BaseCoverageAnnotator;
import consulo.execution.test.autotest.AbstractAutoTestManager;
import consulo.extensionPreviewRecorder.impl.ModuleExtensionPreviewRecorder;
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileEditor.FileEditorProvider;
import consulo.hub.backend.TempFileService;
import consulo.hub.pluginAnalyzer.Analyzer;
import consulo.hub.pluginAnalyzer.container.ContainerBoot;
import consulo.language.codeStyle.ProjectCodeStyleSettingsManager;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.editor.ui.TreeClassChooserFactory;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeKey;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.extension.MutableModuleExtension;
import consulo.navigation.NavigationItem;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.platform.base.localize.CommonLocalize;
import consulo.process.ExecutionException;
import consulo.project.ProjectManager;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.task.TaskRepositoryType;
import consulo.ui.Component;
import consulo.util.collection.Maps;
import consulo.util.collection.primitive.ints.IntMaps;
import consulo.util.collection.trove.impl.TroveCollectionFactory;
import consulo.util.dataholder.UserDataHolder;
import consulo.util.lang.ObjectUtil;
import consulo.util.nodep.ArrayUtilRt;
import consulo.util.xml.serializer.XmlSerializerUtil;
import consulo.versionControlSystem.AbstractVcs;
import consulo.versionControlSystem.VcsFactory;
import consulo.versionControlSystem.distributed.branch.DvcsSyncSettings;
import consulo.versionControlSystem.log.VcsLogObjectsFactory;
import consulo.virtualFileSystem.internal.VirtualFileTracker;
import gnu.trove.TIntIntHashMap;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @since 05/05/2023
 */
public class PluginAnalyzerEnv {
    private static final Logger LOG = LoggerFactory.getLogger(PluginAnalyzerEnv.class);

    private PluginAnalyzerClassGroup myContainerGroup = new PluginAnalyzerClassGroup();
    private PluginAnalyzerClassGroup myPlatformClassGroup = new PluginAnalyzerClassGroup();
    private PluginAnalyzerClassGroup myAnalyzerClassGroup = new PluginAnalyzerClassGroup();

    private final TempFileService myTempFileService;

    public PluginAnalyzerEnv(TempFileService tempFileService) {
        myTempFileService = tempFileService;
    }

    private void initClasses() {
        // container-api
        myContainerGroup.requireClass(PluginDescriptor.class);
        // container-impl
        myContainerGroup.requireClass(ContainerLogger.class);
        // util-nodep
        myContainerGroup.requireClass(ArrayUtilRt.class);
        // boot
        myContainerGroup.requireClass(ContainerBoot.class);
        // gson - for external runner
        myContainerGroup.requireClass(Gson.class);

        // application-impl
        myPlatformClassGroup.requireClass(PluginsLoader.class);
        // application-api
        myPlatformClassGroup.requireClass(Application.class);
        // component-api
        myPlatformClassGroup.requireClass(ComponentManager.class);
        // component-impl
        myPlatformClassGroup.requireClass(BaseComponentManager.class);
        // logging-api
        myPlatformClassGroup.requireClass(consulo.logging.Logger.class);
        // logging-impl
        myPlatformClassGroup.requireClass(consulo.logging.internal.LoggerFactory.class);
        // disposer-api
        myPlatformClassGroup.requireClass(Disposer.class);
        // disposer-impl
        myPlatformClassGroup.requireClass(DisposerInternalImpl.class);
        // trove collection impl
        myPlatformClassGroup.requireClass(TroveCollectionFactory.class);
        // data-hoder-api
        myPlatformClassGroup.requireClass(UserDataHolder.class);
        // ui-api
        myPlatformClassGroup.requireClass(Component.class);
        // util-collections-primitive
        myPlatformClassGroup.requireClass(IntMaps.class);
        // util-collection
        myPlatformClassGroup.requireClass(Maps.class);
        // util-lang
        myPlatformClassGroup.requireClass(ObjectUtil.class);
        // trove
        myPlatformClassGroup.requireClass(TIntIntHashMap.class);
        // slf4j
        myPlatformClassGroup.requireClass(Logger.class);
        // annotation
        myPlatformClassGroup.requireClass(ComponentScope.class);
        // jakarta api
        myPlatformClassGroup.requireClass(Provider.class);
        // component store impl
        myPlatformClassGroup.requireClass(IComponentStore.class);
        // vfs api
        myPlatformClassGroup.requireClass(VirtualFileTracker.class);
        // application-content-api
        myPlatformClassGroup.requireClass(BinariesOrderRootType.class);
        // execution-api
        myPlatformClassGroup.requireClass(ConfigurationType.class);
        // compiler-artifact-api
        myPlatformClassGroup.requireClass(ArtifactType.class);
        // compiler api
        myPlatformClassGroup.requireClass(Compiler.class);
        // vcs api
        myPlatformClassGroup.requireClass(AbstractVcs.class);
        // task api
        myPlatformClassGroup.requireClass(TaskRepositoryType.class);
        // module content api
        myPlatformClassGroup.requireClass(ModuleExtensionProvider.class);
        // base localize
        myPlatformClassGroup.requireClass(CommonLocalize.class);
        // localize
        myPlatformClassGroup.requireClass(LocalizeKey.class);
        // project api
        myPlatformClassGroup.requireClass(ProjectManager.class);
        // language api
        myPlatformClassGroup.requireClass(LanguageFileType.class);
        // language editor api
        myPlatformClassGroup.requireClass(CodeInsightActionHandler.class);
        // file chooser api
        myPlatformClassGroup.requireClass(FileChooserDescriptor.class);
        // module api
        myPlatformClassGroup.requireClass(MutableModuleExtension.class);
        // serializer util
        myPlatformClassGroup.requireClass(XmlSerializerUtil.class);
        // base icon group
        myPlatformClassGroup.requireClass(PlatformIconGroup.class);
        // process api
        myPlatformClassGroup.requireClass(ExecutionException.class);
        // file editor api
        myPlatformClassGroup.requireClass(FileEditorProvider.class);
        // navigation api
        myPlatformClassGroup.requireClass(NavigationItem.class);
        // document api
        myPlatformClassGroup.requireClass(FileDocumentManager.class);
        // vcs api
        myPlatformClassGroup.requireClass(VcsFactory.class);
        // dvcs-api
        myPlatformClassGroup.requireClass(DvcsSyncSettings.class);
        // execution coverage
        myPlatformClassGroup.requireClass(BaseCoverageAnnotator.class);
        // external-system-api
        myPlatformClassGroup.requireClass(AbstractExternalSystemSettings.class);
        // project-ui
        myPlatformClassGroup.requireClass(ToolWindowManager.class);
        // language-code-style-api
        myPlatformClassGroup.requireClass(ProjectCodeStyleSettingsManager.class);
        // execution-test-api
        myPlatformClassGroup.requireClass(AbstractAutoTestManager.class);
        // vcs-log-api
        myPlatformClassGroup.requireClass(VcsLogObjectsFactory.class);
        // language-editor-ui api
        myPlatformClassGroup.requireClass(TreeClassChooserFactory.class);

        // recorders impl
        myAnalyzerClassGroup.requireClass(ModuleExtensionPreviewRecorder.class);
        // analyzer rt
        myAnalyzerClassGroup.requireClass(Analyzer.class);
    }

    public void init() throws Exception {
        initClasses();

        myContainerGroup.init(myTempFileService);

        myPlatformClassGroup.init(myTempFileService);

        myAnalyzerClassGroup.init(myTempFileService);
    }

    public PluginAnalyzerClassGroup getContainerGroup() {
        return myContainerGroup;
    }

    public PluginAnalyzerClassGroup getPlatformClassGroup() {
        return myPlatformClassGroup;
    }

    public PluginAnalyzerClassGroup getAnalyzerClassGroup() {
        return myAnalyzerClassGroup;
    }
}
