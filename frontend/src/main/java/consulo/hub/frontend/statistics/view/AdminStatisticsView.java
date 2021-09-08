package consulo.hub.frontend.statistics.view;

import com.ejt.vaadin.sizereporter.SizeReporter;
import com.intellij.util.ArrayUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import consulo.hub.frontend.backend.service.BackendStatisticsService;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.errorReporter.ui.ScrollableListPanel;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import consulo.hub.shared.statistics.domain.StatisticUsageGroup;
import consulo.hub.shared.statistics.domain.StatisticUsageGroupValue;
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
import org.dussan.vaadin.dcharts.options.*;
import org.dussan.vaadin.dcharts.renderers.tick.AxisTickRenderer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
@SpringView(name = AdminStatisticsView.ID)
public class AdminStatisticsView extends VerticalLayout implements View
{
	public static final String ID = "adminStatistics";

	@Autowired
	private BackendStatisticsService myStatisticRepository;

	public AdminStatisticsView()
	{
		setMargin(false);
		setSpacing(false);
		setSizeFull();
		setDefaultComponentAlignment(Alignment.TOP_LEFT);
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
		header.addStyleName("headerMargin");
		header.setWidth(100, Unit.PERCENTAGE);
		header.addComponent(new Label("Last Stastistics"));

		addComponent(header);

		List<StatisticEntry> all = myStatisticRepository.listAll();

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
				StatisticsGroup statGroup = merged.computeIfAbsent(usageGroup.getUsageGroupId(), StatisticsGroup::new);

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
		addComponent(label);

		ScrollableListPanel scrollable = new ScrollableListPanel();
		addComponent(scrollable);
		setExpandRatio(scrollable, 1);

		for(Map.Entry<String, StatisticsGroup> e : merged.entrySet())
		{
			Panel panel = new Panel(e.getKey(), buildChart(e.getValue()));

			scrollable.add(panel);
		}
	}

	private Component buildChart(StatisticsGroup group)
	{
		Map<String, AtomicInteger> counts = group.getCounts();

		DataSeries dataSeries = new DataSeries().add(reverse(counts.values()));

		SeriesDefaults seriesDefaults = new SeriesDefaults().setFillToZero(true).setRenderer(SeriesRenderers.BAR);
		Series series = new Series().addSeries(new XYseries().setLabel("Count"));
		Axes axes = new Axes().addAxis(new XYaxis().setRenderer(AxisRenderers.CATEGORY).setTicks(new Ticks().add(reverse(counts.keySet())))).addAxis(new XYaxis(XYaxes.Y4).setTickOptions(new
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

		return customComponent;
	}

	private static Object[] reverse(Collection<?> collection)
	{
		Object[] objects = collection.toArray();
		return ArrayUtil.reverseArray(objects);
	}
}
