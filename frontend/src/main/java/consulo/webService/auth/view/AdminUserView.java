package consulo.webService.auth.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.intellij.openapi.util.text.StringUtil;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.auth.mongo.domain.Role;
import consulo.webService.auth.mongo.domain.UserAccount;
import consulo.webService.auth.mongo.repository.UserAccountRepository;

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

		Table table = new Table();
		table.setSizeFull();
		table.addContainerProperty("Email", String.class, null);
		table.addContainerProperty("Status", String.class, null);
		table.addContainerProperty("Roles", String.class, null);

		for(UserAccount userAccount : list)
		{
			table.addItem(new Object[]{
					userAccount.getUsername(),
					userAccount.getStatus(),
					StringUtil.join(userAccount.getRoles(), Role::getId, ", ")
			}, userAccount.getUsername());
		}
		addComponent(table);
		setExpandRatio(table, 1);
	}
}
