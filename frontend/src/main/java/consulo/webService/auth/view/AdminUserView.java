package consulo.webService.auth.view;

import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.auth.domain.UserAccount;
import consulo.webService.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * @author VISTALL
 * @since 25-Jan-17
 */
@SpringView(name = AdminUserView.ID)
public class AdminUserView extends VerticalLayout implements View
{
	public static final String ID = "adminUser";

	private UserAccountRepository myUserAccountRepository;

	@Autowired
	public AdminUserView(UserAccountRepository userAccountRepository)
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

		List<UserAccount> list = myUserAccountRepository.findAll();
		Label label = new Label("Users: " + list.size());
		label.addStyleName("headerMargin");
		addComponent(label);

		Grid<UserAccount> table = new Grid<>();
		table.setSizeFull();
		table.setDataProvider(new ListDataProvider<>(list));

		table.addColumn(UserAccount::getUsername).setCaption("Email");
		table.addColumn(UserAccount::getStatus).setCaption("Status");
		table.addColumn(userAccount -> StringUtil.join(userAccount.getAuthorities(), GrantedAuthority::getAuthority, ", ")).setCaption("Roles");

		addComponent(table);
		setExpandRatio(table, 1);
	}
}
