package consulo.hub.frontend.auth.view;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.hub.frontend.auth.service.UserAccountService;
import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 25-Jan-17
 */
@SpringView(name = AdminUserView.ID)
public class AdminUserView extends VerticalLayout implements View
{
	public static final String ID = "adminUser";

	private UserAccountService myUserAccountRepository;

	@Autowired
	public AdminUserView(UserAccountService userAccountRepository)
	{
		myUserAccountRepository = userAccountRepository;
		setSpacing(false);
		setMargin(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		List<UserAccount> list = myUserAccountRepository.listAll();
		Label label = new Label("Users: " + list.size());
		label.addStyleName("headerMargin");
		addComponent(label);

		Grid<UserAccount> table = new Grid<>();
		table.setSizeFull();
		table.setDataProvider(new ListDataProvider<>(list));

		table.addColumn(UserAccount::getUsername).setCaption("Email");
		table.addColumn(UserAccount::getStatus).setCaption("Status");
		table.addColumn(userAccount -> userAccount.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", "))).setCaption("Roles");

		addComponent(table);
		setExpandRatio(table, 1);
	}
}
