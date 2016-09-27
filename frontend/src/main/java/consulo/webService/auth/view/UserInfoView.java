package consulo.webService.auth.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@SpringView(name = UserInfoView.ID)
public class UserInfoView extends VerticalLayout implements View
{
	public static final String ID = "userInfo";

	public UserInfoView()
	{
		setMargin(true);
		addComponent(new Label("User Info"));
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		// NOP
	}
}
