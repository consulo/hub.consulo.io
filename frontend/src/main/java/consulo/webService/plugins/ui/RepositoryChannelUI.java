package consulo.webService.plugins.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.VersionComparatorUtil;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.plugins.PluginNode;
import consulo.webService.ui.RepositoryUI;
import consulo.webService.ui.util.TidyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public class RepositoryChannelUI extends HorizontalLayout
{
	private final UserConfigurationService myUserConfigurationService;

	private final HorizontalLayout myRightLayout;

	public RepositoryChannelUI(Page page, @NotNull PluginChannel pluginChannel, @NotNull UserConfigurationService userConfigurationService, @Nullable String selectedPluginId)
	{
		myUserConfigurationService = userConfigurationService;

		setSizeFull();

		ListSelect listSelect = new ListSelect();
		listSelect.setNullSelectionAllowed(false);
		listSelect.setSizeFull();
		addComponent(listSelect);

		myRightLayout = new HorizontalLayout();
		myRightLayout.setSizeFull();

		addComponent(myRightLayout);
		setExpandRatio(listSelect, .3f);
		setExpandRatio(myRightLayout, 1f);

		HorizontalSplitPanel panel = new HorizontalSplitPanel();
		panel.setSplitPosition(80, Unit.PERCENTAGE);
		panel.setSizeFull();
		myRightLayout.addComponent(panel);

		PluginChannelService repositoryByChannel = myUserConfigurationService.getRepositoryByChannel(pluginChannel);

		Multimap<String, PluginNode> multimap = ArrayListMultimap.create();
		repositoryByChannel.iteratePluginNodes(pluginNode -> multimap.put(pluginNode.id, pluginNode));

		// name -> id
		Map<PluginNode, String> map = new TreeMap<>((o1, o2) -> {
			int i = o1.name.compareTo(o2.name);
			if(PluginChannelService.isPlatformNode(o1.id))
			{
				return -1;
			}
			else if(PluginChannelService.isPlatformNode(o2.id))
			{
				return 1;
			}
			return i;
		});
		for(Map.Entry<String, Collection<PluginNode>> entry : multimap.asMap().entrySet())
		{
			map.put(entry.getValue().iterator().next(), entry.getKey());
		}

		for(Map.Entry<PluginNode, String> entry : map.entrySet())
		{
			listSelect.addItem(entry.getValue());
			listSelect.setItemCaption(entry.getValue(), getPluginNodeName(entry.getKey()));
		}

		listSelect.addValueChangeListener(event -> {
			String pluginId = (String) event.getProperty().getValue();

			page.setUriFragment(RepositoryUI.getUrlFragment(pluginChannel, pluginId));

			// all plugin nodes
			Collection<PluginNode> pluginNodes = multimap.get(pluginId);

			// version -> nodes
			Comparator<PluginNode> pluginNodeComparator = (o1, o2) -> VersionComparatorUtil.compare(o2.version, o1.version);
			SortedSetMultimap<String, PluginNode> sortByVersion = TreeMultimap.create(Collections.reverseOrder(StringUtil::naturalCompare), pluginNodeComparator);

			for(PluginNode pluginNode : pluginNodes)
			{
				sortByVersion.put(pluginNode.platformVersion, pluginNode);
			}

			Map<String, Collection<PluginNode>> sorted = sortByVersion.asMap();

			Tree tree = new Tree("Versions");
			for(Map.Entry<String, Collection<PluginNode>> entry : sorted.entrySet())
			{
				tree.addItem(entry.getKey());
				tree.setItemCaption(entry.getKey(), "Consulo #" + entry.getKey());

				for(PluginNode node : entry.getValue())
				{
					UUID uuid = UUID.randomUUID();

					tree.addItem(uuid);
					tree.setItemCaption(uuid, "build #" + node.version);

					tree.setParent(uuid, entry.getKey());
					tree.setChildrenAllowed(uuid, false);
				}
			}

			panel.setFirstComponent(buildInfo(pluginNodes));
			panel.setSecondComponent(tree);
		});

		if(selectedPluginId != null)
		{
			listSelect.setValue(selectedPluginId);
		}
	}

	private Component buildInfo(Collection<PluginNode> collection)
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);

		PluginNode next = collection.iterator().next();

		verticalLayout.addComponent(VaadinUIUtil.labeled("ID: ", TidyComponents.newLabel(next.id)));
		verticalLayout.addComponent(VaadinUIUtil.labeled("Name: ", TidyComponents.newLabel(getPluginNodeName(next))));
		verticalLayout.addComponent(VaadinUIUtil.labeled("Category: ", TidyComponents.newLabel(next.category)));
		if(!StringUtil.isEmpty(next.vendor))
		{
			verticalLayout.addComponent(VaadinUIUtil.labeled("Vendor: ", TidyComponents.newLabel(next.vendor)));
		}

		if(!StringUtil.isEmpty(next.description))
		{
			TextArea area = new TextArea();
			area.setValue(next.description);
			area.setReadOnly(true);
			area.setWidth(100, Unit.PERCENTAGE);
			area.addStyleName(ValoTheme.TEXTAREA_SMALL);
			area.addStyleName(ValoTheme.TEXTAREA_BORDERLESS);
			verticalLayout.addComponent(area);
		}

		return verticalLayout;
	}

	private static String getPluginNodeName(PluginNode pluginNode)
	{
		if(PluginChannelService.isPlatformNode(pluginNode.id))
		{
			switch(pluginNode.id)
			{
				// windows
				case "consulo-win-no-jre":
					return "Platform (Windows, without JRE)";
				case "consulo-win":
					return "Platform (Windows, with JRE x32)";
				case "consulo-win64":
					return "Platform (Windows, with JRE x64)";
				case "consulo-win-no-jre-zip":
					return "Platform (Windows, without JRE, zip archive)";
				case "consulo-win-zip":
					return "Platform (Windows, with JRE x32, zip archive)";
				case "consulo-win64-zip":
					return "Platform (Windows, with JRE x64, zip archive)";
				// linux
				case "consulo-linux-no-jre":
					return "Platform (Linux, without JRE)";
				case "consulo-linux":
					return "Platform (Linux, with JRE x32)";
				case "consulo-linux64":
					return "Platform (Linux, with JRE x64)";
				// mac
				case "consulo-mac-no-jre":
					return "Platform (macOS, without JRE)";
				case "consulo-mac64":
					return "Platform (macOS, with JRE x64)";
				default:
					return pluginNode.id;
			}
		}
		return pluginNode.name;
	}
}
