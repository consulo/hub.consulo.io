package consulo.webService.auth.view;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.ui.util.TinyComponents;

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
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return;
		}

		Label label = new Label("Profile");
		label.addStyleName("headerMargin");
		addComponent(label);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.addStyleName("bodyMargin");

		verticalLayout.addComponent(TinyComponents.newLabel("Not Implemented Yet"));

		addComponent(verticalLayout);
		setExpandRatio(verticalLayout, 1f);
	}
}
