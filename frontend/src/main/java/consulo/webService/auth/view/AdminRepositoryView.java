package consulo.webService.auth.view;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import com.intellij.util.ui.UIUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelIterationService;
import consulo.webService.ui.util.TidyComponents;

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

		setMargin(true);

		addComponent(TidyComponents.newLabel("Force iteration: "));

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.addComponent(TidyComponents.newButton("nightly " + UIUtil.rightArrow() + " alpha", event -> forceIterate(PluginChannel.nightly, PluginChannel.alpha)));
		layout.addComponent(TidyComponents.newButton("alpha " + UIUtil.rightArrow() + " beta", event -> forceIterate(PluginChannel.alpha, PluginChannel.beta)));
		layout.addComponent(TidyComponents.newButton("beta " + UIUtil.rightArrow() + " release", event -> forceIterate(PluginChannel.beta, PluginChannel.release)));

		addComponent(layout);
	}

	private void forceIterate(@NotNull PluginChannel from, @NotNull PluginChannel to)
	{
		myTaskExecutor.execute(() -> myPluginChannelIterationService.iterate(from, to));
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
	}
}
