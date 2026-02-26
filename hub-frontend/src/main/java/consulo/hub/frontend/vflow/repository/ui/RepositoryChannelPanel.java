package consulo.hub.frontend.vflow.repository.ui;

import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import consulo.hub.frontend.vflow.backend.service.BackendPluginStatisticsService;
import consulo.hub.frontend.vflow.backend.service.BackendRepositoryService;
import consulo.hub.frontend.vflow.repository.view.RepositoryView;
import consulo.hub.shared.repository.FrontPluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.procoeton.core.backend.BackendServiceDownException;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.procoeton.core.vaadin.util.Notifications;
import consulo.procoeton.core.vaadin.util.ProcoetonStyles;
import consulo.procoeton.core.vaadin.util.RouterUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.VersionComparatorUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author VISTALL
 * @since 2017-01-04
 */
public class RepositoryChannelPanel extends HorizontalLayout {
    private static final Comparator<FrontPluginNode> ourPluginNodeComparator =
        (o1, o2) -> VersionComparatorUtil.compare(o2.version(), o1.version());

    private final BackendPluginStatisticsService myBackendPluginStatisticsService;
    private final TagsLocalizeLoader myTagsLocalizeLoader;
    private final Multimap<String, FrontPluginNode> myPluginBuilds;
    private final ListBox<FrontPluginNode> myListSelect;
    private String mySelectedPluginId;

    private HorizontalLayout myHolder;

    public RepositoryChannelPanel(
        @Nonnull BackendRepositoryService backendRepositoryService,
        @Nonnull BackendPluginStatisticsService backendPluginStatisticsService,
        @Nonnull TagsLocalizeLoader tagsLocalizeLoader,
        RouteParameters routeParameters
    ) {
        myBackendPluginStatisticsService = backendPluginStatisticsService;
        myTagsLocalizeLoader = tagsLocalizeLoader;

        setSizeFull();

        myListSelect = new ListBox<>();
        myListSelect.setHeightFull();
        myListSelect.setWidth(35, Unit.EM);

        add(myListSelect);

        myHolder = VaadinUIUtil.newHorizontalLayout();
        myHolder.setWidthFull();

        // we have own scrolling
        myHolder.addClassName(ProcoetonStyles.Overflow.AUTO);

        add(myHolder);
        setFlexGrow(1f, myHolder);

        myPluginBuilds = TreeMultimap.create(Collections.<String>reverseOrder(Comparator.<String>naturalOrder()), ourPluginNodeComparator);
        try {
            backendRepositoryService.listAll(node -> myPluginBuilds.put(node.id(), node));
        }
        catch (BackendServiceDownException ignored) {
            Notifications.serverOffline();
        }

        List<FrontPluginNode> items = new ArrayList<>();
        for (Map.Entry<String, Collection<FrontPluginNode>> entry : myPluginBuilds.asMap().entrySet()) {
            Collection<FrontPluginNode> value = entry.getValue();
            items.add(ContainerUtil.getFirstItem(value));
        }

        items.sort((o1, o2) -> {
            if (RepositoryUtil.isPlatformNode(o1.id())) {
                return -1;
            }
            else if (RepositoryUtil.isPlatformNode(o2.id())) {
                return 1;
            }

            return o1.name().compareToIgnoreCase(o2.name());
        });

        myListSelect.setItems(items);

        myListSelect.setRenderer(new ComponentRenderer<Component, FrontPluginNode>((c) -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            String iconBytes = c.myPluginNode.iconBytes;
            Image image = new Image();

            if (RepositoryUtil.isPlatformNode(c.id())) {
                InputStream platformIcon = getClass().getResourceAsStream("/images/consuloBig.svg");
                image.setSrc(new StreamResource(c.id() + ".svg", (InputStreamFactory)() -> platformIcon));
            }
            else if (iconBytes == null) {
                InputStream pluginIcon = getClass().getResourceAsStream("/images/pluginBig.svg");
                image.setSrc(new StreamResource(c.id() + ".svg", (InputStreamFactory)() -> pluginIcon));
            }
            else {
                byte[] imgBytes = Base64.getDecoder().decode(iconBytes);

                image.setSrc(new StreamResource(c.id() + ".svg", (InputStreamFactory)() -> new ByteArrayInputStream(imgBytes)));
            }

            image.setHeight(3, Unit.EM);
            image.setWidth(3, Unit.EM);
            row.add(image);

            row.add(new Span(c.name()));
            return row;
        }));

        myListSelect.addValueChangeListener(event -> {
            FrontPluginNode node = event.getValue();

            String pluginId = node == null ? null : node.id();

            mySelectedPluginId = pluginId;

            selectPlugin(pluginId);

            myHolder.removeAll();

            if (!StringUtils.isEmpty(pluginId)) {
                Component component = build(pluginId);
                if (component != null) {
                    myHolder.add(component);
                }
            }

            RouterUtil.updateUrl(RepositoryView.class, () -> routeParameters, Map.of(RepositoryView.ID, pluginId));
        });
    }

    public void selectPlugin(@Nullable String pluginId) {
        if (pluginId == null) {
            myListSelect.setValue(null);
        }
        else {
            Collection<FrontPluginNode> nodes = myPluginBuilds.get(pluginId);
            if (nodes.isEmpty()) {
                myListSelect.setValue(null);
            }
            else {
                myListSelect.setValue(ContainerUtil.getFirstItem(nodes));
            }
        }
    }

    @Nullable
    public String getSelectedPluginId() {
        return mySelectedPluginId;
    }

    private Component build(String pluginId) {
        // all plugin nodes
        Collection<FrontPluginNode> pluginNodes = myPluginBuilds.get(pluginId);

        // version -> nodes
        SortedSetMultimap<String, FrontPluginNode> sortByVersion =
            TreeMultimap.create(Collections.reverseOrder(StringUtil::naturalCompare), ourPluginNodeComparator);

        for (FrontPluginNode pluginNode : pluginNodes) {
            sortByVersion.put(pluginNode.platformVersion(), pluginNode);
        }

        FrontPluginNode lastPluginNodeByVersion = null;

        Map<String, Collection<FrontPluginNode>> sorted = sortByVersion.asMap();

        for (Map.Entry<String, Collection<FrontPluginNode>> entry : sorted.entrySet()) {
            lastPluginNodeByVersion = entry.getValue().iterator().next();
            break;
        }

        if (lastPluginNodeByVersion != null) {
            return new RepositoryItemComponent(lastPluginNodeByVersion, myTagsLocalizeLoader, myBackendPluginStatisticsService, sorted);
        }

        return null;
    }
}
