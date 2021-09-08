package consulo.hub.frontend.repository.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.hub.frontend.backend.service.BackendRepositoryService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.shared.repository.PluginChannel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05-Jan-17
 */
@SpringView(name = AdminRepositoryView.ID)
public class AdminRepositoryView extends VerticalLayout implements View
{
	public static final String ID = "adminRepository";

	private BackendRepositoryService myBackendRepositoryService;

	@Autowired
	public AdminRepositoryView(BackendRepositoryService backendRepositoryService)
	{
		myBackendRepositoryService = backendRepositoryService;

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
		layout.addComponent(TinyComponents.newButton("nightly " + rightArrow() + " alpha", event -> forceIterate(PluginChannel.nightly, PluginChannel.alpha)));
		layout.addComponent(TinyComponents.newButton("alpha " + rightArrow() + " beta", event -> forceIterate(PluginChannel.alpha, PluginChannel.beta)));
		layout.addComponent(TinyComponents.newButton("beta " + rightArrow() + " release", event -> forceIterate(PluginChannel.beta, PluginChannel.release)));

		addComponent(layout);
		setExpandRatio(layout, 1);
	}

	@Nonnull
	private static String rightArrow()
	{
		return "->";
	}

	private void forceIterate(@Nonnull PluginChannel from, @Nonnull PluginChannel to)
	{
		myBackendRepositoryService.iteratePlugins(from, to);
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
	}
}
