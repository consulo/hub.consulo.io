package consulo.hub.frontend.vflow.repository.ui;

import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.service.BackendPluginStatisticsService;
import consulo.hub.frontend.vflow.backend.service.BackendRepositoryService;
import consulo.hub.frontend.vflow.base.util.VaadinUIUtil;
import consulo.hub.frontend.vflow.repository.view.RepositoryView;
import consulo.hub.frontend.vflow.util.RouterUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.VersionComparatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public class RepositoryChannelPanel extends HorizontalLayout
{
	private static final Comparator<PluginNode> ourPluginNodeComparator = (o1, o2) -> VersionComparatorUtil.compare(o2.version, o1.version);

	private final BackendPluginStatisticsService myBackendPluginStatisticsService;
	private final TagsLocalizeLoader myTagsLocalizeLoader;
	private final PluginChannel myPluginChannel;
	private final Multimap<String, PluginNode> myPluginBuilds;
	private final ListBox<String> myListSelect;
	private String mySelectedPluginId;

	private Map<PluginNode, String> myNameToIdMap;

	private HorizontalLayout myHolder;

	public RepositoryChannelPanel(@Nonnull PluginChannel pluginChannel,
								  @Nonnull BackendRepositoryService backendRepositoryService,
								  @Nonnull BackendPluginStatisticsService backendPluginStatisticsService,
								  @Nonnull TagsLocalizeLoader tagsLocalizeLoader, RouteParameters routeParameters)
	{
		myPluginChannel = pluginChannel;
		myBackendPluginStatisticsService = backendPluginStatisticsService;
		myTagsLocalizeLoader = tagsLocalizeLoader;

		setSizeFull();

		myListSelect = new ListBox<>();
		myListSelect.setHeightFull();
		myListSelect.setWidth(35, Unit.PERCENTAGE);
		add(myListSelect);

		myHolder = VaadinUIUtil.newHorizontalLayout();
		myHolder.setWidthFull();

		// we have own scrolling
		myHolder.addClassName(LumoUtility.Overflow.AUTO);

		add(myHolder);
		setFlexGrow(1f, myHolder);

		myPluginBuilds = TreeMultimap.<String, PluginNode>create(Collections.<String>reverseOrder(Comparator.<String>naturalOrder()), ourPluginNodeComparator);
		backendRepositoryService.listAll(pluginChannel, pluginNode -> myPluginBuilds.put(pluginNode.id, pluginNode));

		// name -> id
		myNameToIdMap = new TreeMap<>((o1, o2) ->
		{
			if(RepositoryUtil.isPlatformNode(o1.id))
			{
				return -1;
			}
			else if(RepositoryUtil.isPlatformNode(o2.id))
			{
				return 1;
			}
			else if(RepositoryUtil.isPlatformNode(o1.id) && RepositoryUtil.isPlatformNode(o2.id))
			{
				return RepositoryItemComponent.getPluginNodeName(o1).compareToIgnoreCase(RepositoryItemComponent.getPluginNodeName(o2));
			}
			return o1.name.compareToIgnoreCase(o2.name);
		});

		for(Map.Entry<String, Collection<PluginNode>> entry : myPluginBuilds.asMap().entrySet())
		{
			myNameToIdMap.put(entry.getValue().iterator().next(), entry.getKey());
		}

		myListSelect.setItems(myNameToIdMap.values());

		Map<String, String> captions = new HashMap<>();
		for(Map.Entry<PluginNode, String> entry : myNameToIdMap.entrySet())
		{
			captions.put(entry.getValue(), RepositoryItemComponent.getPluginNodeName(entry.getKey()));
		}
		myListSelect.setItemLabelGenerator(captions::get);

		myListSelect.addValueChangeListener(event ->
		{
			String pluginId = event.getValue();

			mySelectedPluginId = pluginId;

			selectPlugin(pluginId);

			myHolder.removeAll();

			if(!StringUtils.isEmpty(pluginId))
			{
				Component component = build(pluginId);
				if(component != null)
				{
					myHolder.add(component);
				}
			}

			RouterUtil.updateUrl(RepositoryView.class, () -> routeParameters, Map.of(RepositoryView.CHANNEL, pluginChannel.name(), RepositoryView.ID, pluginId));
		});
	}

	public void selectPlugin(@Nullable String pluginId)
	{
		if(pluginId == null || !myNameToIdMap.containsValue(pluginId))
		{
			myListSelect.setValue(null);
		}
		else
		{
			myListSelect.setValue(pluginId);
		}
	}

	@Nullable
	public String getSelectedPluginId()
	{
		return mySelectedPluginId;
	}

	private Component build(String pluginId)
	{
		// all plugin nodes
		Collection<PluginNode> pluginNodes = myPluginBuilds.get(pluginId);

		// version -> nodes
		SortedSetMultimap<String, PluginNode> sortByVersion = TreeMultimap.create(Collections.reverseOrder(StringUtil::naturalCompare), ourPluginNodeComparator);

		for(PluginNode pluginNode : pluginNodes)
		{
			sortByVersion.put(pluginNode.platformVersion, pluginNode);
		}

		PluginNode lastPluginNodeByVersion = null;

		Map<String, Collection<PluginNode>> sorted = sortByVersion.asMap();

		Map<String, PluginNode> downloadInfo = new HashMap<>();

		Map<String, String> captions = new HashMap<>();

		for(Map.Entry<String, Collection<PluginNode>> entry : sorted.entrySet())
		{
			captions.put(entry.getKey(), "Consulo #" + entry.getKey());

			if(lastPluginNodeByVersion == null)
			{
				lastPluginNodeByVersion = entry.getValue().iterator().next();
			}

			if(!RepositoryUtil.isPlatformNode(lastPluginNodeByVersion.id))
			{
				for(PluginNode node : entry.getValue())
				{
					UUID uuid = UUID.randomUUID();

					String key = uuid.toString();

					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(node.date);
					captions.put(key, "build #" + node.version + " at " + calendar.toInstant());

					downloadInfo.put(key, node);
				}
			}
			else
			{
				downloadInfo.put(entry.getKey(), entry.getValue().iterator().next());
			}
		}


		//tree.setItemCaptionGenerator(captions::get);
		//
		//		tree.addItemClickListener(e ->
		//		{
		//			String id = e.getItem();
		//
		//			PluginNode pluginNode = downloadInfo.get(id);
		//			if(pluginNode != null)
		//			{
		//				StringBuilder builder = new StringBuilder("/api/repository/download?");
		//				builder.append("channel=").append(myPluginChannel.name()).append("&");
		//				builder.append("platformVersion=").append(pluginNode.platformVersion).append("&");
		//				builder.append("id=").append(pluginNode.id).append("&");
		//				builder.append("version=").append(pluginNode.version).append("&");
		//				builder.append("platformBuildSelect=").append("true");
		//
		//				getUI().getPage().open(builder.toString(), "");
		//			}
		//		});
		//
		//		assert lastPluginNodeByVersion != null;
		if(lastPluginNodeByVersion != null)
		{
			return new RepositoryItemComponent(lastPluginNodeByVersion, myPluginChannel, myTagsLocalizeLoader, myBackendPluginStatisticsService, sorted);
		}

		return null;
	}
}