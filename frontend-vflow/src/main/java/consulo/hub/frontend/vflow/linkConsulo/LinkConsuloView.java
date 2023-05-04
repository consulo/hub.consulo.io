package consulo.hub.frontend.vflow.linkConsulo;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.*;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.ui.util.TinyComponents;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.vaadin.view.CenteredView;
import jakarta.annotation.security.PermitAll;

import java.util.List;

/**
 * @author VISTALL
 * @since 04/09/2021
 */
@PageTitle("Linking Consulo")
@PermitAll
@Route(value = "link", layout = MainLayout.class)
public class LinkConsuloView extends CenteredView
{
	private final BackendUserAccountService myUserAccountService;

	public LinkConsuloView(BackendUserAccountService userAccountService)
	{
		myUserAccountService = userAccountService;
	}

	@Override
	protected String getHeaderText()
	{
		return "Linking";
	}

	@Override
	protected void fill(VerticalLayout layout, Location location)
	{
		UserAccount userAccout = SecurityUtil.getUserAccout();
		if(userAccout == null)
		{
			return;
		}

		layout.add(new Label("Do you want link Consulo to this account?"));

		Button yes = TinyComponents.newButton("Yes", clickEvent -> {
			QueryParameters queryParameters = location.getQueryParameters();

			String redirect = getFirstParameter(queryParameters, "redirect");
			String token = getFirstParameter(queryParameters, "token");
			String hostName = getFirstParameter(queryParameters, "host");

			myUserAccountService.requestOAuthKey(userAccout, token, hostName);

			Page current = UI.getCurrent().getPage();
			current.open(redirect + "?token=" + token, "Redirect");

			//FIXME ?? current.setUriFragment(DashboardView.ID);
		});
		yes.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		Button no = TinyComponents.newButton("No", clickEvent -> {
			UI.getCurrent().getPage().executeJs("window.close()");
		});

		HorizontalLayout buttonsLine = new HorizontalLayout();
		buttonsLine.add(yes, no);

		layout.add(buttonsLine);
	}

	private String getFirstParameter(QueryParameters queryParameters, String name)
	{
		List<String> values = queryParameters.getParameters().get(name);
		if(values == null || values.size() != 1)
		{
			return null;
		}
		return values.get(0);
	}
}
