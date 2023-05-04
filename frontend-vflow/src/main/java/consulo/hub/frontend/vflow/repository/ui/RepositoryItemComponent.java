package consulo.hub.frontend.vflow.repository.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.service.BackendPluginStatisticsService;
import consulo.procoeton.core.vaadin.ui.Badge;
import consulo.procoeton.core.vaadin.ui.util.TinyComponents;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import consulo.hub.shared.repository.util.RepositoryUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class RepositoryItemComponent extends VerticalLayout
{
	RepositoryItemComponent(@Nonnull PluginNode pluginNode,
							@Nonnull PluginChannel pluginChannel,
							@Nonnull TagsLocalizeLoader tagsLocalizeLoader,
							@Nonnull BackendPluginStatisticsService backendPluginStatisticsService,
							@Nonnull Map<String, Collection<PluginNode>> versions)
	{
		setSizeFull();
		setPadding(false);
		setMargin(false);

		add(VaadinUIUtil.labeled("ID: ", TinyComponents.newLabel(pluginNode.id)));
		add(VaadinUIUtil.labeled("Name: ", TinyComponents.newLabel(getPluginNodeName(pluginNode))));
		if(pluginNode.experimental)
		{
			Label label = TinyComponents.newLabel("EXPERIMENTAL");
			label.addClassName(LumoUtility.BorderColor.ERROR);
			label.addClassName(LumoUtility.Border.ALL);
			label.addClassName(LumoUtility.BorderRadius.MEDIUM);
			label.addClassName(LumoUtility.TextColor.ERROR);
			label.addClassName(LumoUtility.Padding.SMALL);
			add(label);
		}
		HorizontalLayout tagsPanel = new HorizontalLayout();
		if(pluginNode.tags != null)
		{
			for(String tag : pluginNode.tags)
			{
				Badge label = new Badge(tagsLocalizeLoader.getTagLocalize(tag));
				tagsPanel.add(label);
			}
		}
		add(tagsPanel);
		add(VaadinUIUtil.newHorizontalLayout(TinyComponents.newLabel("Permission:")));
		PluginNode.Permission[] permissions = pluginNode.permissions;
		if(permissions != null)
		{
			for(PluginNode.Permission permission : permissions)
			{
				Label label = TinyComponents.newLabel("- " + permission.type);
				add(VaadinUIUtil.newHorizontalLayout(label));
			}
		}
		else
		{
			Label label = TinyComponents.newLabel("- <no special permissions>");
			add(VaadinUIUtil.newHorizontalLayout(label));
		}

		if(!StringUtils.isEmpty(pluginNode.description))
		{
			Html descriptiopnLabel = new Html("<div>" + pluginNode.description + "</div>");
			descriptiopnLabel.addClassName(LumoUtility.FontSize.XSMALL);
			descriptiopnLabel.addClassName(LumoUtility.Width.FULL);
			//descriptiopnLabel.addClassName(LumoUtility.Padding.SMALL);
			//descriptiopnLabel.addClassName(LumoUtility.Border.ALL);
			//descriptiopnLabel.addClassName(LumoUtility.BorderRadius.SMALL);
			//descriptiopnLabel.addClassName(LumoUtility.BorderColor.CONTRAST_10);
			descriptiopnLabel.addClassName(LumoUtility.Background.CONTRAST_20);

			add(descriptiopnLabel);
		}

		if(!StringUtils.isEmpty(pluginNode.vendor))
		{
			add(VaadinUIUtil.labeled("Vendor: ", TinyComponents.newLabel(pluginNode.vendor)));
		}

		RepositoryDownloadInfo[] allDownloadStat = backendPluginStatisticsService.getDownloadStat(pluginNode.id);
		List<RepositoryDownloadInfo> channelDownloadStat = Arrays.stream(allDownloadStat).filter(it -> it.getChannel().equals(pluginChannel.name())).collect(Collectors.toList());

		add(VaadinUIUtil.labeled("Downloads: ", TinyComponents.newLabel(channelDownloadStat.size() + " (all: " + allDownloadStat.length + ")")));

		add(TinyComponents.newLabel("Download statistics"));

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

			for(RepositoryDownloadInfo mongoDownloadStat : channelDownloadStat)
			{
				if(mongoDownloadStat.getTime() >= start && mongoDownloadStat.getTime() <= end)
				{
					downloads++;
				}
			}

			String format = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
			data.put(format, downloads);
		}

		// TODO charts
		//		DataSeries dataSeries = new DataSeries().add(reverse(data.values()));
		//
		//		SeriesDefaults seriesDefaults = new SeriesDefaults().setFillToZero(true).setRenderer(SeriesRenderers.BAR);
		//		Series series = new Series().addSeries(new XYseries().setLabel("Downloads"));
		//		Axes axes = new Axes().addAxis(new XYaxis().setRenderer(AxisRenderers.CATEGORY).setTicks(new Ticks().add(reverse(data.keySet())))).addAxis(new XYaxis(XYaxes.Y).setTickOptions(new
		//				AxisTickRenderer().setFormatString("%d")));
		//
		//		Highlighter highlighter = new Highlighter().setShow(true).setShowTooltip(true).setTooltipAlwaysVisible(true).setKeepTooltipInsideChart(true).setTooltipLocation(TooltipLocations.NORTH)
		//				.setTooltipAxes(TooltipAxes.XY_BAR);
		//
		//		Options options = new Options().setHighlighter(highlighter).setSeriesDefaults(seriesDefaults).setSeries(series).setAxes(axes);
		//		DCharts chart = new DCharts().setDataSeries(dataSeries).setOptions(options).show();
		//		// show component on first resize
		//		chart.setVisible(false);
		//		chart.setHeight(20, Unit.EM);
		//
		//		CustomComponent customComponent = new CustomComponent(chart);
		//		customComponent.setSizeFull();
		//
		//		SizeReporter sizeReporter = new SizeReporter(customComponent);
		//		sizeReporter.addResizeListener(componentResizeEvent ->
		//		{
		//			chart.setVisible(true);
		//			chart.setWidth(componentResizeEvent.getWidth(), Unit.PIXELS);
		//		});
		//
		//		verticalLayout.addComponent(customComponent);
		//		verticalLayout.setExpandRatio(customComponent, 1.f);

		TabSheet tabSheet = new TabSheet();
		tabSheet.setWidthFull();
		add(tabSheet);

		tabSheet.add("Versions", buildVersion(versions));
		tabSheet.add("Comments", new VerticalLayout());
	}

	private Component buildVersion(Map<String, Collection<PluginNode>> versions)
	{
		Accordion accordion = new Accordion();

		for(Map.Entry<String, Collection<PluginNode>> entry : versions.entrySet())
		{
			String key = entry.getKey();

			Collection<PluginNode> value = entry.getValue();

			VerticalLayout layout = VaadinUIUtil.newVerticalLayout();

			for(PluginNode node : value)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(node.date);

				HorizontalLayout row = VaadinUIUtil.newHorizontalLayout();
				row.add("build #" + node.version + " at " + calendar.toInstant());
				row.add(new Button("Download"));

				layout.add(row);
			}

			AccordionPanel panel = accordion.add("Consulo #" + key, layout);
			panel.setOpened(true);
		}
		return accordion;
	}

	private static Object[] reverse(Collection<?> collection)
	{
		Object[] objects = collection.toArray();
		ArrayUtils.reverse(objects);
		return objects;
	}

	public static String getPluginNodeName(PluginNode pluginNode)
	{
		if(RepositoryUtil.isPlatformNode(pluginNode.id))
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
