package consulo.webService.plugins.ui;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.dussan.vaadin.dcharts.DCharts;
import org.dussan.vaadin.dcharts.base.elements.XYaxis;
import org.dussan.vaadin.dcharts.base.elements.XYseries;
import org.dussan.vaadin.dcharts.data.DataSeries;
import org.dussan.vaadin.dcharts.data.Ticks;
import org.dussan.vaadin.dcharts.metadata.TooltipAxes;
import org.dussan.vaadin.dcharts.metadata.XYaxes;
import org.dussan.vaadin.dcharts.metadata.locations.TooltipLocations;
import org.dussan.vaadin.dcharts.metadata.renderers.AxisRenderers;
import org.dussan.vaadin.dcharts.metadata.renderers.SeriesRenderers;
import org.dussan.vaadin.dcharts.options.Axes;
import org.dussan.vaadin.dcharts.options.Highlighter;
import org.dussan.vaadin.dcharts.options.Options;
import org.dussan.vaadin.dcharts.options.Series;
import org.dussan.vaadin.dcharts.options.SeriesDefaults;
import org.dussan.vaadin.dcharts.renderers.tick.AxisTickRenderer;

import javax.annotation.Nullable;
import com.ejt.vaadin.sizereporter.SizeReporter;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.text.DateFormatUtilRt;
import com.intellij.util.text.VersionComparatorUtil;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.plugins.PluginNode;
import consulo.webService.plugins.PluginStatisticsService;
import consulo.webService.plugins.mongo.MongoDownloadStat;
import consulo.webService.plugins.view.RepositoryView;
import consulo.webService.ui.util.TinyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 04-Jan-17
 */
public class RepositoryChannelPanel extends HorizontalLayout
{
	private static final Comparator<PluginNode> ourPluginNodeComparator = (o1, o2) -> VersionComparatorUtil.compare(o2.version, o1.version);

	private final HorizontalSplitPanel myPanel = new HorizontalSplitPanel();
	private final PluginStatisticsService myPluginStatisticsService;
	private final PluginChannel myPluginChannel;
	private final Multimap<String, PluginNode> myPluginBuilds;
	private final ListSelect<String> myListSelect;
	private String mySelectedPluginId;

	private Map<PluginNode, String> myNameToIdMap;

	public RepositoryChannelPanel(@Nonnull PluginChannel pluginChannel, @Nonnull UserConfigurationService userConfigurationService, @Nonnull PluginStatisticsService pluginStatisticsService)
	{
		myPluginChannel = pluginChannel;
		myPluginStatisticsService = pluginStatisticsService;

		setSizeFull();

		myListSelect = new ListSelect<>();
		myListSelect.setSizeFull();
		addComponent(myListSelect);

		HorizontalLayout rightLayout = VaadinUIUtil.newHorizontalLayout();
		rightLayout.setSizeFull();

		addComponent(rightLayout);
		setExpandRatio(myListSelect, .3f);
		setExpandRatio(rightLayout, 1f);

		myPanel.setSplitPosition(80, Unit.PERCENTAGE);
		rightLayout.addComponent(myPanel);

		PluginChannelService repositoryByChannel = userConfigurationService.getRepositoryByChannel(pluginChannel);

		myPluginBuilds = TreeMultimap.create(Collections.reverseOrder(StringUtil::naturalCompare), ourPluginNodeComparator);
		repositoryByChannel.iteratePluginNodes(pluginNode -> myPluginBuilds.put(pluginNode.id, pluginNode));

		// name -> id
		myNameToIdMap = new TreeMap<>((o1, o2) ->
		{
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

		for(Map.Entry<String, Collection<PluginNode>> entry : myPluginBuilds.asMap().entrySet())
		{
			myNameToIdMap.put(entry.getValue().iterator().next(), entry.getKey());
		}

		ListDataProvider<String> provider = new ListDataProvider<>(myNameToIdMap.values());
		myListSelect.setDataProvider(provider);

		Map<String, String> captions = new HashMap<>();
		for(Map.Entry<PluginNode, String> entry : myNameToIdMap.entrySet())
		{
			captions.put(entry.getValue(), getPluginNodeName(entry.getKey()));
		}
		myListSelect.setItemCaptionGenerator(captions::get);

		myListSelect.addValueChangeListener(event ->
		{
			String pluginId = event.getValue().iterator().next();

			mySelectedPluginId = pluginId;

			getUI().getNavigator().navigateTo(RepositoryView.ID + "/" + RepositoryView.getViewParameters(pluginChannel, pluginId));
		});
	}

	public void selectPlugin(@Nullable String pluginId)
	{
		mySelectedPluginId = pluginId;

		myListSelect.focus();
		if(pluginId == null || !myNameToIdMap.containsValue(pluginId))
		{
			myListSelect.setValue(Collections.emptySet());
		}
		else
		{
			myListSelect.setValue(Sets.newHashSet(pluginId));
		}

		if(StringUtil.isEmpty(pluginId))
		{
			myPanel.setFirstComponent(null);
			myPanel.setSecondComponent(null);
		}
		else
		{
			buildPluginInfo(pluginId);
		}
	}

	@Nullable
	public String getSelectedPluginId()
	{
		return mySelectedPluginId;
	}

	private void buildPluginInfo(String pluginId)
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

		TreeData<String> treeData = new TreeData<>();
		for(Map.Entry<String, Collection<PluginNode>> entry : sorted.entrySet())
		{
			treeData.addRootItems(entry.getKey());
			captions.put(entry.getKey(), "Consulo #" + entry.getKey());

			if(lastPluginNodeByVersion == null)
			{
				lastPluginNodeByVersion = entry.getValue().iterator().next();
			}

			if(!PluginChannelService.isPlatformNode(lastPluginNodeByVersion.id))
			{
				for(PluginNode node : entry.getValue())
				{
					UUID uuid = UUID.randomUUID();

					String key = uuid.toString();
					treeData.addItem(entry.getKey(), key);

					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(node.date);
					captions.put(key, "build #" + node.version + " at " + DateFormatUtilRt.formatBuildDate(calendar));

					downloadInfo.put(key, node);
				}
			}
			else
			{
				downloadInfo.put(entry.getKey(), entry.getValue().iterator().next());
			}
		}

		Tree<String> tree = new Tree<>("Versions", treeData);
		tree.setItemCaptionGenerator(captions::get);

		tree.addItemClickListener(e ->
		{
			String id = e.getItem();

			PluginNode pluginNode = downloadInfo.get(id);
			if(pluginNode != null)
			{
				StringBuilder builder = new StringBuilder("/api/repository/download?");
				builder.append("channel=").append(myPluginChannel.name()).append("&");
				builder.append("platformVersion=").append(pluginNode.platformVersion).append("&");
				builder.append("id=").append(pluginNode.id).append("&");
				builder.append("version=").append(pluginNode.version).append("&");
				builder.append("platformBuildSelect=").append("true");

				getUI().getPage().open(builder.toString(), "");
			}
		});

		assert lastPluginNodeByVersion != null;
		myPanel.setFirstComponent(buildInfo(lastPluginNodeByVersion));
		myPanel.setSecondComponent(tree);
	}

	@Nullable
	private Component buildInfo(@Nullable PluginNode pluginNode)
	{
		if(pluginNode == null)
		{
			return null;
		}

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);
		verticalLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		verticalLayout.setSizeFull();

		verticalLayout.addComponent(VaadinUIUtil.labeled("ID: ", TinyComponents.newLabel(pluginNode.id)));
		verticalLayout.addComponent(VaadinUIUtil.labeled("Name: ", TinyComponents.newLabel(getPluginNodeName(pluginNode))));
		verticalLayout.addComponent(VaadinUIUtil.labeled("Category: ", TinyComponents.newLabel(pluginNode.category)));
		if(!StringUtil.isEmpty(pluginNode.vendor))
		{
			verticalLayout.addComponent(VaadinUIUtil.labeled("Vendor: ", TinyComponents.newLabel(pluginNode.vendor)));
		}

		if(!StringUtil.isEmpty(pluginNode.description))
		{
			Label descriptiopnLabel = new Label();
			descriptiopnLabel.setContentMode(ContentMode.HTML);
			descriptiopnLabel.setValue(pluginNode.description);
			descriptiopnLabel.setWidth(100, Unit.PERCENTAGE);
			descriptiopnLabel.addStyleName(ValoTheme.LABEL_SMALL);

			CssLayout customComponent = new CssLayout(descriptiopnLabel);
			customComponent.setWidth(100, Unit.PERCENTAGE);
			customComponent.addStyleName(ValoTheme.LAYOUT_WELL);
			verticalLayout.addComponent(customComponent);
		}

		List<MongoDownloadStat> allDownloadStat = myPluginStatisticsService.getDownloadStat(pluginNode.id);
		List<MongoDownloadStat> channelDownloadStat = allDownloadStat.stream().filter(it -> it.getChannel().equals(myPluginChannel.name())).collect(Collectors.toList());

		verticalLayout.addComponent(VaadinUIUtil.labeled("Downloads: ", TinyComponents.newLabel(channelDownloadStat.size() + " (all: " + allDownloadStat.size() + ")")));

		verticalLayout.addComponent(TinyComponents.newLabel("Download statistics"));

		LocalDate now = LocalDate.now();

		Map<String, Long> data = new LinkedHashMap<>();
		for(int i = 0; i < 12; i++)
		{
			LocalDate month = now.minusMonths(i);
			month = month.with(TemporalAdjusters.firstDayOfMonth());

			long downloads = 0;
			long start = month.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

			month = month.with(TemporalAdjusters.lastDayOfMonth());
			long end = month.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

			for(MongoDownloadStat mongoDownloadStat : channelDownloadStat)
			{
				if(mongoDownloadStat.getTime() >= start && mongoDownloadStat.getTime() <= end)
				{
					downloads++;
				}
			}

			String format = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
			data.put(format, downloads);
		}

		DataSeries dataSeries = new DataSeries().add(reverse(data.values()));

		SeriesDefaults seriesDefaults = new SeriesDefaults().setFillToZero(true).setRenderer(SeriesRenderers.BAR);
		Series series = new Series().addSeries(new XYseries().setLabel("Downloads"));
		Axes axes = new Axes().addAxis(new XYaxis().setRenderer(AxisRenderers.CATEGORY).setTicks(new Ticks().add(reverse(data.keySet())))).addAxis(new XYaxis(XYaxes.Y).setTickOptions(new
				AxisTickRenderer().setFormatString("%d")));

		Highlighter highlighter = new Highlighter().setShow(true).setShowTooltip(true).setTooltipAlwaysVisible(true).setKeepTooltipInsideChart(true).setTooltipLocation(TooltipLocations.NORTH)
				.setTooltipAxes(TooltipAxes.XY_BAR);

		Options options = new Options().setHighlighter(highlighter).setSeriesDefaults(seriesDefaults).setSeries(series).setAxes(axes);
		DCharts chart = new DCharts().setDataSeries(dataSeries).setOptions(options).show();
		// show component on first resize
		chart.setVisible(false);
		chart.setHeight(20, Unit.EM);

		CustomComponent customComponent = new CustomComponent(chart);
		customComponent.setSizeFull();

		SizeReporter sizeReporter = new SizeReporter(customComponent);
		sizeReporter.addResizeListener(componentResizeEvent ->
		{
			chart.setVisible(true);
			chart.setWidth(componentResizeEvent.getWidth(), Unit.PIXELS);
		});

		verticalLayout.addComponent(customComponent);
		verticalLayout.setExpandRatio(customComponent, 1.f);

		return new Panel(verticalLayout);
	}

	private static Object[] reverse(Collection<?> collection)
	{
		Object[] objects = collection.toArray();
		return ArrayUtil.reverseArray(objects);
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
