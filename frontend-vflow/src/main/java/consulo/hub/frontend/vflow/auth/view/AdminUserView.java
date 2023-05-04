package consulo.hub.frontend.vflow.auth.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.hub.shared.auth.Roles;
import consulo.hub.shared.auth.domain.UserAccount;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 25-Jan-17
 */
@PageTitle("Admin Users")
@Route(value = "admin/users", layout = MainLayout.class)
@RolesAllowed(Roles.ROLE_SUPERUSER)
public class AdminUserView extends VChildLayout
{
	private BackendUserAccountService myUserAccountRepository;

	@Autowired
	public AdminUserView(BackendUserAccountService userAccountRepository)
	{
		myUserAccountRepository = userAccountRepository;
		setSpacing(false);
		setMargin(false);
		setSizeFull();
	}

	@Override
	public void viewReady(AfterNavigationEvent afterNavigationEvent)
	{
		removeAll();

		List<UserAccount> list = myUserAccountRepository.listAll();

		Grid<UserAccount> table = new Grid<>();
		table.setSizeFull();
		table.setDataProvider(new ListDataProvider<>(list));

		table.addColumn(UserAccount::getUsername).setHeader("Email");
		table.addColumn(UserAccount::getStatus).setHeader("Status");
		table.addColumn(userAccount -> userAccount.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", "))).setHeader("Roles");

		add(table);
	}
}
