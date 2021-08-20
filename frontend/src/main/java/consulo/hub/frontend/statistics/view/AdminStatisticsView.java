package consulo.hub.frontend.statistics.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.ejt.vaadin.sizereporter.SizeReporter;
import com.intellij.util.ArrayUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import consulo.hub.frontend.errorReporter.ui.ScrollableListPanel;
import consulo.hub.shared.statistics.domain.StatisticBean;
import consulo.webService.statistics.mongo.StatisticRepository;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
@SpringView(name = AdminStatisticsView.ID)
public class AdminStatisticsView extends VerticalLayout implements View
{
	public static final String ID = "adminStatistics";

	@Autowired
	private StatisticRepository myStatisticRepository;

	@Autowired
	private MongoTemplate myMongoTemplate;

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

		List<StatisticBean> all = myStatisticRepository.findAll(new Sort(Sort.Direction.ASC, "createTime"));

		Map<String, StatisticBean> group = new HashMap<>();

		// TODO [VISTALL] we need it do via mongo?
		for(StatisticBean bean : all)
		{
			StatisticBean old = group.get(bean.getInstallationID());

			if(old == null || old.getCreateTime() > bean.getCreateTime())
			{
				group.put(bean.getInstallationID(), bean);
			}
		}

		final int installationCount = group.size();

		Map<String, StatisticsGroup> merged = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		for(StatisticBean bean : group.values())
		{
			for(StatisticBean.UsageGroup usageGroup : bean.getGroups())
			{
				StatisticsGroup statGroup = merged.computeIfAbsent(usageGroup.getId(), StatisticsGroup::new);

				for(StatisticBean.UsageGroupValue values : usageGroup.getValues())
				{
					statGroup.incData(values.getId(), values.getCount());
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

		Highlighter highlighter = new Highlighter().setShow(true).setShowTooltip(true).setTooltipAlwaysVisible(true).setKeepTooltipInsideChart(true).setTooltipLocation(TooltipLocations.NORTH).setTooltipAxes(TooltipAxes.XY_BAR);

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
