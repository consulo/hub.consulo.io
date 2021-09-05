package consulo.hub.frontend.linkConsulo;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.backend.service.BackendUserAccountService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.dash.view.DashboardView;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 04/09/2021
 */
@SpringView(name = LinkConsuloView.ID)
public class LinkConsuloView extends VerticalLayout implements View
{
	public static final String ID = "linkConsulo";

	@Autowired
	private BackendUserAccountService myUserAccountService;

	public LinkConsuloView()
	{
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		UserAccount userAccout = SecurityUtil.getUserAccout();
		if(userAccout == null)
		{
			return;
		}

		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
		header.setWidth(100, Unit.PERCENTAGE);
		header.addComponent(new Label("Linking Consulo"));
		header.addStyleName("headerMargin");

		addComponent(header);

		VerticalLayout list = VaadinUIUtil.newVerticalLayout();
		list.setSpacing(true);
		list.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		list.addStyleName("bodyMargin");

		addComponent(list);
		setExpandRatio(list, 1);


		Button yes = TinyComponents.newButton("Yes", clickEvent -> {
			String redirect = event.getParameterMap().get("redirect");
			String token = event.getParameterMap().get("token");
			String hostName = event.getParameterMap().get("host");

			myUserAccountService.requestOAuthKey(userAccout, token, hostName);

			Page current = Page.getCurrent();
			current.open(redirect + "?token=" + token, "Redirect");

			current.setUriFragment(DashboardView.ID);
		});
		yes.addStyleName(ValoTheme.BUTTON_PRIMARY);

		Button no = TinyComponents.newButton("No", clickEvent -> {
			Page.getCurrent().getJavaScript().execute("window.close()");
		});

		HorizontalLayout buttonsLine = new HorizontalLayout();
		buttonsLine.addComponents(yes, no);

		list.addComponent(new Label("Do you want link Consulo to this account?"));
		list.addComponent(buttonsLine);
	}
}
