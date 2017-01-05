package consulo.webService.plugins.ui;

import java.text.DateFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.DateFormatUtilRt;
import com.intellij.util.text.VersionComparatorUtil;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.calendar.event.BasicEvent;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.plugins.PluginNode;
import consulo.webService.plugins.PluginStatisticsService;
import consulo.webService.plugins.mongo.MongoDownloadStat;
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
	private final PluginStatisticsService myPluginStatisticsService;
	private final PluginChannel myPluginChannel;

	private final HorizontalLayout myRightLayout;

	public RepositoryChannelUI(Page page,
			@NotNull PluginChannel pluginChannel,
			@NotNull UserConfigurationService userConfigurationService,
			@NotNull PluginStatisticsService pluginStatisticsService,
			@Nullable String selectedPluginId)
	{
		myPluginChannel = pluginChannel;
		myUserConfigurationService = userConfigurationService;
		myPluginStatisticsService = pluginStatisticsService;

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
		panel.setSplitPosition(70, Unit.PERCENTAGE);
		panel.setSizeFull();
		myRightLayout.addComponent(panel);

		PluginChannelService repositoryByChannel = myUserConfigurationService.getRepositoryByChannel(pluginChannel);

		Comparator<PluginNode> pluginNodeComparator = (o1, o2) -> VersionComparatorUtil.compare(o2.version, o1.version);
		Multimap<String, PluginNode> multimap =TreeMultimap.create(Collections.reverseOrder(StringUtil::naturalCompare), pluginNodeComparator);
		repositoryByChannel.iteratePluginNodes(pluginNode -> multimap.put(pluginNode.id, pluginNode));

		// name -> id
		Map<PluginNode, String> map = new TreeMap<>((o1, o2) -> {
			if(PluginChannelService.isPlatformNode(o1.id))
			{
				return -1;
			}
			else if(PluginChannelService.isPlatformNode(o2.id))
			{
				return 1;
			}
			else if(PluginChannelService.isPlatformNode(o1.id) && PluginChannelService.isPlatformNode(o2.id))
			{
				return getPluginNodeName(o1).compareToIgnoreCase(getPluginNodeName(o2));
			}
			return o1.name.compareToIgnoreCase(o2.name);
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
			SortedSetMultimap<String, PluginNode> sortByVersion = TreeMultimap.create(Collections.reverseOrder(StringUtil::naturalCompare), pluginNodeComparator);

			for(PluginNode pluginNode : pluginNodes)
			{
				sortByVersion.put(pluginNode.platformVersion, pluginNode);
			}

			PluginNode lastPluginNode = null;

			Map<String, Collection<PluginNode>> sorted = sortByVersion.asMap();

			Tree tree = new Tree("Versions");
			for(Map.Entry<String, Collection<PluginNode>> entry : sorted.entrySet())
			{
				tree.addItem(entry.getKey());
				tree.setItemCaption(entry.getKey(), "Consulo #" + entry.getKey());

				lastPluginNode = entry.getValue().iterator().next();
				if(!PluginChannelService.isPlatformNode(lastPluginNode.id))
				{
					for(PluginNode node : entry.getValue())
					{
						UUID uuid = UUID.randomUUID();

						tree.addItem(uuid);

						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(node.date);
						tree.setItemCaption(uuid, "build #" + node.version + " at " + DateFormatUtilRt.formatBuildDate(calendar));

						tree.setParent(uuid, entry.getKey());
						tree.setChildrenAllowed(uuid, false);
					}
				}
				else
				{
					tree.setChildrenAllowed(entry.getKey(), false);
				}
			}

			assert lastPluginNode != null;
			panel.setFirstComponent(buildInfo(lastPluginNode));
			panel.setSecondComponent(tree);
		});

		if(selectedPluginId != null)
		{
			listSelect.setValue(selectedPluginId);
		}
	}

	@NotNull
	private Component buildInfo(@NotNull PluginNode pluginNode)
	{
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);

		verticalLayout.addComponent(VaadinUIUtil.labeled("ID: ", TidyComponents.newLabel(pluginNode.id)));
		verticalLayout.addComponent(VaadinUIUtil.labeled("Name: ", TidyComponents.newLabel(getPluginNodeName(pluginNode))));
		verticalLayout.addComponent(VaadinUIUtil.labeled("Category: ", TidyComponents.newLabel(pluginNode.category)));
		if(!StringUtil.isEmpty(pluginNode.vendor))
		{
			verticalLayout.addComponent(VaadinUIUtil.labeled("Vendor: ", TidyComponents.newLabel(pluginNode.vendor)));
		}

		if(!StringUtil.isEmpty(pluginNode.description))
		{
			TextArea area = new TextArea();
			area.setValue(pluginNode.description);
			area.setReadOnly(true);
			area.setWidth(100, Unit.PERCENTAGE);
			area.addStyleName(ValoTheme.TEXTAREA_SMALL);
			area.addStyleName(ValoTheme.TEXTAREA_BORDERLESS);
			verticalLayout.addComponent(area);
		}

		List<MongoDownloadStat> allDownloadStat = myPluginStatisticsService.getDownloadStat(pluginNode.id);
		List<MongoDownloadStat> channelDownloadStat = allDownloadStat.stream().filter(it -> it.getChannel().equals(myPluginChannel.name())).collect(Collectors.toList());

		verticalLayout.addComponent(VaadinUIUtil.labeled("Downloads: ", TidyComponents.newLabel(channelDownloadStat.size() + " (all: " + allDownloadStat.size() + ")")));

		com.vaadin.ui.Calendar calendar = new com.vaadin.ui.Calendar()
		{
			protected String[] getDayNamesShort()
			{
				DateFormatSymbols s = new DateFormatSymbols(getLocale());
				return Arrays.copyOfRange(s.getShortWeekdays(), 1, 8);
			}
		};
		calendar.setWidth(25, Unit.EM);
		calendar.setHeight(18, Unit.EM);

		for(MongoDownloadStat mongoDownloadStat : channelDownloadStat)
		{
			calendar.addEvent(new BasicEvent("download", "", new Date(mongoDownloadStat.getTime())));
		}

		HorizontalLayout calendarControl = new HorizontalLayout();
		calendarControl.setSpacing(true);

		calendarControl.addComponent(TidyComponents.newLabel("Download statistics"));
		calendarControl.addComponent(TidyComponents.newButton("Month view", event -> switchToMonthView(calendar)));

		verticalLayout.addComponent(calendarControl);

		switchToMonthView(calendar);

		verticalLayout.addComponent(calendar);

		return verticalLayout;
	}

	private void switchToMonthView(com.vaadin.ui.Calendar calendarComponent)
	{
		Calendar calendar = Calendar.getInstance();
		int rollAmount = calendar.get(GregorianCalendar.DAY_OF_MONTH) - 1;
		calendar.add(GregorianCalendar.DAY_OF_MONTH, -rollAmount);

		calendarComponent.setStartDate(calendar.getTime());

		calendar.add(GregorianCalendar.MONTH, 1);
		calendar.add(GregorianCalendar.DATE, -1);

		calendarComponent.setEndDate(calendar.getTime());
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
