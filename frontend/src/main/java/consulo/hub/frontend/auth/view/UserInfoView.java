package consulo.hub.frontend.auth.view;

import com.vaadin.data.ValueContext;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import consulo.hub.frontend.auth.service.UserAccountService;
import consulo.hub.frontend.auth.ui.LoginOrRegisterPanel;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@SpringView(name = UserInfoView.ID)
public class UserInfoView extends VerticalLayout implements View
{
	public static final String ID = "userInfo";

	@Autowired
	private UserAccountService myUserAccountService;

	public UserInfoView()
	{
		setSizeFull();
		setSpacing(false);
		setMargin(false);
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

		VerticalLayout verticalLayout = VaadinUIUtil.newVerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.addStyleName("bodyMargin");

		VerticalLayout changePasswordLayout = new VerticalLayout();
		Panel panel = new Panel("Change Password", changePasswordLayout);

		PasswordField oldPasswordField = new PasswordField();
		oldPasswordField.setId("old-password");
		changePasswordLayout.addComponent(VaadinUIUtil.labeledFill("Old Password", oldPasswordField));
		PasswordField newPasswordField = new PasswordField();
		newPasswordField.setId("new-password");
		changePasswordLayout.addComponent(VaadinUIUtil.labeledFill("New Password", newPasswordField));
		PasswordField newPasswordField2 = new PasswordField();
		newPasswordField2.setId("new-password2");
		changePasswordLayout.addComponent(VaadinUIUtil.labeledFill("New Password", newPasswordField2));
		changePasswordLayout.addComponent(TinyComponents.newButton("Change Password", clickEvent ->
				changePassword(authentication, oldPasswordField.getValue(), newPasswordField.getValue(), newPasswordField2.getValue())));

		verticalLayout.addComponent(panel);

		addComponent(verticalLayout);
		setExpandRatio(verticalLayout, 1f);
	}

	private void changePassword(Authentication authentication, String oldPassword, String newPassword, String newPassword2)
	{
		ValueContext valueContext = new ValueContext();
		if(LoginOrRegisterPanel.ourPasswordValidator.apply(oldPassword, valueContext).isError() || LoginOrRegisterPanel.ourPasswordValidator.apply(newPassword, valueContext).isError() ||
				LoginOrRegisterPanel.ourPasswordValidator.apply(newPassword2, valueContext).isError())
		{
			error("Password is bad");
			return;
		}

		if(!newPassword.equals(newPassword2))
		{
			error("New passwords not equal");
			return;
		}

		if(oldPassword.equals(newPassword2))
		{
			error("Old password equals new password");
			return;
		}

		if(!myUserAccountService.changePassword(authentication.getName(), oldPassword, newPassword))
		{
			error("Failed to change password");
			return;
		}

		info("Password changed");
	}

	private static void info(String message)
	{
		Notification notification = new Notification(message, Notification.Type.TRAY_NOTIFICATION);
		notification.setPosition(Position.TOP_RIGHT);
		notification.setDelayMsec(3000);
		notification.show(Page.getCurrent());
	}

	private static void error(String message)
	{
		Notification notification = new Notification(message, Notification.Type.ERROR_MESSAGE);
		notification.setPosition(Position.TOP_RIGHT);
		notification.setDelayMsec(3000);
		notification.show(Page.getCurrent());
	}
}
