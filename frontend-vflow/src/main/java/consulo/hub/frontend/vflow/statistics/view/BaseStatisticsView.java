package consulo.hub.frontend.vflow.statistics.view;

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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.AfterNavigationEvent;
import consulo.hub.frontend.vflow.backend.service.BackendStatisticsService;
import consulo.procoeton.core.vaadin.ui.ScrollableLayout;
import consulo.hub.frontend.vflow.repository.ui.RepositoryItemComponent;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import consulo.hub.shared.statistics.domain.StatisticUsageGroup;
import consulo.hub.shared.statistics.domain.StatisticUsageGroupValue;
import consulo.procoeton.core.vaadin.ui.LabeledLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.util.lang.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author VISTALL
 * @since 09/09/2021
 */
public abstract class BaseStatisticsView extends VChildLayout
{
	@Autowired
	protected BackendStatisticsService myStatisticRepository;

	public BaseStatisticsView()
	{
	}

	protected abstract List<StatisticEntry> getStatistics();

	@Override
	public void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		removeAll();

		//		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
		//		header.addStyleName("headerMargin");
		//		header.setWidth(100, Unit.PERCENTAGE);
		//		header.addComponent(new Label("Last Stastistics"));
		//
		//		addComponent(header);

		List<StatisticEntry> all = getStatistics();

		Map<String, StatisticEntry> group = new HashMap<>();

		for(StatisticEntry bean : all)
		{
			StatisticEntry old = group.get(bean.getInstallationID());

			if(old == null || old.getCreateTime() > bean.getCreateTime())
			{
				group.put(bean.getInstallationID(), bean);
			}
		}

		final int installationCount = group.size();

		Map<String, StatisticsGroup> merged = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		for(StatisticEntry bean : group.values())
		{
			for(StatisticUsageGroup usageGroup : bean.getGroups())
			{
				StatisticsGroup statGroup = merged.computeIfAbsent(ObjectUtil.notNull(usageGroup.getUsageGroupId(), "?"), StatisticsGroup::new);

				for(StatisticUsageGroupValue values : usageGroup.getValues())
				{
					statGroup.incData(values.getUsageGroupValueId(), values.getCount());
				}
			}
		}

		// free resources
		all = null;
		group.clear();
		group = null;

		Label label = new Label("Installations: " + installationCount);
		add(label);

		ScrollableLayout scrollable = new ScrollableLayout();
		add(scrollable);

		for(Map.Entry<String, StatisticsGroup> e : merged.entrySet())
		{
			LabeledLayout panel = new LabeledLayout(e.getKey(), buildChart(e.getValue()));

			scrollable.addItem(panel);
		}
	}

	private Component buildChart(StatisticsGroup group)
	{
		Map<String, AtomicInteger> counts = group.getCounts();

		ApexChartsBuilder builder = new ApexChartsBuilder();
		builder.withChart(ChartBuilder.get()
				.withType(Type.BAR)
				.build())
				.withPlotOptions(PlotOptionsBuilder.get()
						.withBar(BarBuilder.get().withHorizontal(false).build())
						.build())
				.withDataLabels(DataLabelsBuilder.get()
						.withEnabled(false).build())
				.withSeries(new Series<>("Count", SeriesType.COLUMN, RepositoryItemComponent.reverse(counts.values()).toArray()))
				.withXaxis(XAxisBuilder.get().withCategories(RepositoryItemComponent.reverse(counts.keySet())).build());

		ApexCharts charts = builder.build();
		charts.setWidthFull();
		return charts;


		//		DataSeries dataSeries = new DataSeries().add(reverse(counts.values()));
		//
		//		SeriesDefaults seriesDefaults = new SeriesDefaults().setFillToZero(true).setRenderer(SeriesRenderers.BAR);
		//		Series series = new Series().addSeries(new XYseries().setLabel("Count"));
		//		Axes axes = new Axes().addAxis(new XYaxis().setRenderer(AxisRenderers.CATEGORY).setTicks(new Ticks().add(reverse(counts.keySet())))).addAxis(new XYaxis(XYaxes.Y4).setTickOptions(new
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
		//		return customComponent;
	}
}
