package consulo.webService.plugins.view;

import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import com.intellij.util.ui.UIUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelIterationService;
import consulo.webService.ui.util.TinyComponents;
import consulo.webService.ui.util.VaadinUIUtil;

/**
 * @author VISTALL
 * @since 05-Jan-17
 */
@SpringView(name = AdminRepositoryView.ID)
public class AdminRepositoryView extends VerticalLayout implements View
{
	public static final String ID = "adminRepository";

	private TaskExecutor myTaskExecutor;
	private PluginChannelIterationService myPluginChannelIterationService;

	@Autowired
	public AdminRepositoryView(TaskExecutor taskExecutor, PluginChannelIterationService pluginChannelIterationService)
	{
		myTaskExecutor = taskExecutor;
		myPluginChannelIterationService = pluginChannelIterationService;

		setMargin(false);
		setSpacing(false);
		setSizeFull();
		setDefaultComponentAlignment(Alignment.TOP_LEFT);

		Label label = new Label("Force iteration");
		label.addStyleName("headerMargin");
		addComponent(label);

		HorizontalLayout layout = VaadinUIUtil.newHorizontalLayout();
		layout.addStyleName("bodyMargin");
		layout.setSpacing(true);
		layout.addComponent(TinyComponents.newButton("nightly " + UIUtil.rightArrow() + " alpha", event -> forceIterate(PluginChannel.nightly, PluginChannel.alpha)));
		layout.addComponent(TinyComponents.newButton("alpha " + UIUtil.rightArrow() + " beta", event -> forceIterate(PluginChannel.alpha, PluginChannel.beta)));
		layout.addComponent(TinyComponents.newButton("beta " + UIUtil.rightArrow() + " release", event -> forceIterate(PluginChannel.beta, PluginChannel.release)));

		addComponent(layout);
		setExpandRatio(layout, 1);
	}

	private void forceIterate(@Nonnull PluginChannel from, @Nonnull PluginChannel to)
	{
		myTaskExecutor.execute(() -> myPluginChannelIterationService.iterate(from, to));
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
	}
}
