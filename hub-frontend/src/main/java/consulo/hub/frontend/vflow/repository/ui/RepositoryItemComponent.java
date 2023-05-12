package consulo.hub.frontend.vflow.repository.ui;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.ChartBuilder;
import com.github.appreciated.apexcharts.config.builder.DataLabelsBuilder;
import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.plotoptions.builder.BarBuilder;
import com.github.appreciated.apexcharts.config.series.SeriesType;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.service.BackendPluginStatisticsService;
import consulo.hub.shared.repository.PluginChannel;
import consulo.hub.shared.repository.PluginNode;
import consulo.hub.shared.repository.domain.RepositoryDownloadInfo;
import consulo.hub.shared.repository.util.RepositoryUtil;
import consulo.procoeton.core.vaadin.ui.Badge;
import consulo.procoeton.core.vaadin.ui.LazyComponent;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.procoeton.core.vaadin.util.Notifications;
import jakarta.annotation.Nonnull;
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
	private final PluginChannel myPluginChannel;

	RepositoryItemComponent(@Nonnull PluginNode pluginNode,
							@Nonnull PluginChannel pluginChannel,
							@Nonnull TagsLocalizeLoader tagsLocalizeLoader,
							@Nonnull BackendPluginStatisticsService backendPluginStatisticsService,
							@Nonnull Map<String, Collection<PluginNode>> versions)
	{
		myPluginChannel = pluginChannel;
		setSizeFull();
		setPadding(false);
		setMargin(false);

		H3 header = new H3(getPluginNodeName(pluginNode));
		Tooltip.forComponent(header).withText(pluginNode.id);
		add(header);

		if(pluginNode.tags != null && pluginNode.tags.length > 0 || pluginNode.experimental)
		{
			HorizontalLayout tagsPanel = new HorizontalLayout();
			if(pluginNode.experimental)
			{
				tagsPanel.add(new Badge("EXPERIMENTAL", "error"));
			}

			if(pluginNode.tags != null)
			{
				for(String tag : pluginNode.tags)
				{
					Badge label = new Badge(tagsLocalizeLoader.getTagLocalize(tag));
					tagsPanel.add(label);
				}
			}
			add(tagsPanel);
		}

		add(VaadinUIUtil.newHorizontalLayout(new Label("Permission:")));
		PluginNode.Permission[] permissions = pluginNode.permissions;
		if(permissions != null)
		{
			for(PluginNode.Permission permission : permissions)
			{
				Label label = new Label("- " + permission.type);
				add(VaadinUIUtil.newHorizontalLayout(label));
			}
		}
		else
		{
			Label label = new Label("- <no special permissions>");
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
			add(VaadinUIUtil.labeled("Vendor: ", new Label(pluginNode.vendor)));
		}

		add(VaadinUIUtil.labeled("Downloads: ", new Label(pluginNode.downloads + " (all: " + pluginNode.downloadsAll + ")")));

		TabSheet tabSheet = new TabSheet();
		tabSheet.setWidthFull();
		add(tabSheet);

		tabSheet.add("Versions", buildVersion(versions));
		tabSheet.add("Download Statistcs", downloadStatistics(pluginNode, pluginChannel, backendPluginStatisticsService));
		tabSheet.add("Comments", new VerticalLayout());
	}

	private Component downloadStatistics(PluginNode pluginNode, PluginChannel pluginChannel, BackendPluginStatisticsService backendPluginStatisticsService)
	{
		LazyComponent lazyComponent = new LazyComponent(() ->
		{
			RepositoryDownloadInfo[] allDownloadStat = new RepositoryDownloadInfo[0];
			try
			{
				allDownloadStat = backendPluginStatisticsService.getDownloadStat(pluginNode.id);
			}
			catch(Exception e)
			{
				Notifications.serverOffline();
				allDownloadStat = new RepositoryDownloadInfo[0];
			}

			List<RepositoryDownloadInfo> channelDownloadStat = Arrays.stream(allDownloadStat).filter(it -> it.getChannel().equals(pluginChannel.name())).collect(Collectors.toList());

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

				for(RepositoryDownloadInfo downloadInfo : channelDownloadStat)
				{
					if(downloadInfo.getTime() >= start && downloadInfo.getTime() <= end)
					{
						downloads++;
					}
				}

				String format = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
				data.put(format, downloads);
			}

			ApexChartsBuilder builder = new ApexChartsBuilder();
			builder.withChart(ChartBuilder.get()
					.withType(Type.BAR)
					.build())
					.withPlotOptions(PlotOptionsBuilder.get()
							.withBar(BarBuilder.get().withHorizontal(false).build())
							.build())
					.withDataLabels(DataLabelsBuilder.get()
							.withEnabled(false).build())
					.withSeries(new Series<>("Downloads", SeriesType.COLUMN, reverse(data.values()).toArray()))
					.withXaxis(XAxisBuilder.get().withCategories(reverse(data.keySet())).build());

			ApexCharts charts = builder.build();
			charts.setWidthFull();
			return charts;
		});
		lazyComponent.setWidthFull();
		return lazyComponent;
	}

	private Component buildVersion(Map<String, Collection<PluginNode>> versions)
	{
		Accordion accordion = new Accordion();

		for(Map.Entry<String, Collection<PluginNode>> entry : versions.entrySet())
		{
			String key = entry.getKey();

			Collection<PluginNode> value = entry.getValue();

			VerticalLayout layout = VaadinUIUtil.newVerticalLayout();
			layout.setWidthFull();

			for(PluginNode node : value)
			{
				HorizontalLayout row = new HorizontalLayout();
				row.setWidthFull();
				row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
				row.add("build #" + node.version + " at " + new Date(node.date));
				Button downloadButton = new Button("Download #" + node.version, new Icon(VaadinIcon.DOWNLOAD));
				downloadButton.addClickListener(event ->
				{
					StringBuilder builder = new StringBuilder("/api/repository/download?");
					builder.append("channel=").append(myPluginChannel.name()).append("&");
					builder.append("platformVersion=").append(node.platformVersion).append("&");
					builder.append("id=").append(node.id).append("&");
					builder.append("version=").append(node.version).append("&");
					builder.append("platformBuildSelect=").append("true");

					UI.getCurrent().getPage().open(builder.toString(), "");
				});
				downloadButton.addClassName(LumoUtility.Margin.Left.AUTO);
				row.add(downloadButton);
				row.setAlignSelf(Alignment.END, downloadButton);

				layout.add(row);
			}

			AccordionPanel panel = accordion.add("Consulo #" + key, layout);
			panel.setOpened(true);
		}
		return accordion;
	}

	public static <T> List<T> reverse(Collection<T> collection)
	{
		List<T> newList = new ArrayList<T>(collection);
		Collections.reverse(newList);
		return newList;
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
