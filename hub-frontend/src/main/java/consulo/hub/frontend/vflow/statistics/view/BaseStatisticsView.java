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
import com.vaadin.flow.component.html.Span;
import consulo.hub.frontend.vflow.backend.service.BackendStatisticsService;
import consulo.hub.frontend.vflow.repository.ui.RepositoryItemComponent;
import consulo.hub.shared.statistics.domain.StatisticEntry;
import consulo.hub.shared.statistics.domain.StatisticUsageGroup;
import consulo.hub.shared.statistics.domain.StatisticUsageGroupValue;
import consulo.procoeton.core.vaadin.ui.LabeledLayout;
import consulo.procoeton.core.vaadin.ui.ScrollableLayout;
import consulo.procoeton.core.vaadin.ui.ServerOfflineVChildLayout;
import consulo.util.lang.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 09/09/2021
 */
public abstract class BaseStatisticsView extends ServerOfflineVChildLayout {
    @Autowired
    protected BackendStatisticsService myStatisticRepository;

    public BaseStatisticsView() {
        super(true);
    }

    protected abstract List<StatisticEntry> getStatistics();

    @Override
    protected void buildLayout(Consumer<Component> uiBuilder) {
        removeAll();

        //		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
        //		header.addStyleName("headerMargin");
        //		header.setWidth(100, Unit.PERCENTAGE);
        //		header.addComponent(new Label("Last Stastistics"));
        //
        //		addComponent(header);

        List<StatisticEntry> all = getStatistics();

        Map<String, StatisticEntry> group = new HashMap<>();

        for (StatisticEntry bean : all) {
            StatisticEntry old = group.get(bean.getInstallationID());

            if (old == null || old.getCreateTime() > bean.getCreateTime()) {
                group.put(bean.getInstallationID(), bean);
            }
        }

        final int installationCount = group.size();

        Map<String, StatisticsGroup> merged = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (StatisticEntry bean : group.values()) {
            for (StatisticUsageGroup usageGroup : bean.getGroups()) {
                StatisticsGroup statGroup =
                    merged.computeIfAbsent(ObjectUtil.notNull(usageGroup.getUsageGroupId(), "?"), StatisticsGroup::new);

                for (StatisticUsageGroupValue values : usageGroup.getValues()) {
                    statGroup.incData(values.getUsageGroupValueId(), values.getCount());
                }
            }
        }

        // free resources
        all = null;
        group.clear();
        group = null;

        Span label = new Span("Installations: " + installationCount);
        uiBuilder.accept(label);

        ScrollableLayout scrollable = new ScrollableLayout();
        uiBuilder.accept(scrollable);

        for (Map.Entry<String, StatisticsGroup> e : merged.entrySet()) {
            LabeledLayout panel = new LabeledLayout(e.getKey(), buildChart(e.getValue()));

            scrollable.addItem(panel);
        }
    }

    private Component buildChart(StatisticsGroup group) {
        Map<String, AtomicInteger> counts = group.getCounts();

        ApexChartsBuilder builder = new ApexChartsBuilder();
        builder.withChart(
                ChartBuilder.get()
                    .withType(Type.BAR)
                    .build()
            )
            .withPlotOptions(
                PlotOptionsBuilder.get()
                    .withBar(BarBuilder.get().withHorizontal(false).build())
                    .build()
            )
            .withDataLabels(
                DataLabelsBuilder.get()
                    .withEnabled(false).build()
            )
            .withSeries(
                new Series<>("Count", SeriesType.COLUMN, RepositoryItemComponent.reverse(counts.values()).toArray())
            )
            .withXaxis(XAxisBuilder.get().withCategories(RepositoryItemComponent.reverse(counts.keySet())).build());

        ApexCharts charts = builder.build();
        charts.setWidthFull();
        return charts;
    }
}
