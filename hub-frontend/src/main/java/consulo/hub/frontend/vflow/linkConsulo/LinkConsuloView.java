package consulo.hub.frontend.vflow.linkConsulo;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.*;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.vaadin.view.CenteredView;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;

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

		layout.add(new Span("Do you want link Consulo to this account?"));

		ComponentEventListener<ClickEvent<Button>> listener1 = clickEvent -> {
			QueryParameters queryParameters = location.getQueryParameters();

			String redirect = getFirstParameter(queryParameters, "redirect");
			String token = getFirstParameter(queryParameters, "token");
			String hostName = getFirstParameter(queryParameters, "host");

			Map<String, String> map = myUserAccountService.requestOAuthKey(userAccout, token, hostName);

			Page current = UI.getCurrent().getPage();
			current.open(redirect + "?token=" + token, "_blank");

			//FIXME ?? current.setUriFragment(DashboardView.ID);
		};
		Button yes = new Button("Yes", listener1);
		yes.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		ComponentEventListener<ClickEvent<Button>> listener = clickEvent -> {
			UI.getCurrent().getPage().executeJs("window.close()");
		};
		Button no = new Button("No", listener);

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
