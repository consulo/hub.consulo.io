package consulo.hub.frontend.vflow.user.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.backend.service.BackendUserAccountService;
import consulo.hub.shared.auth.SecurityUtil;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.procoeton.core.vaadin.ui.LabeledLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.ui.util.VaadinUIUtil;
import consulo.procoeton.core.vaadin.util.Notifications;
import consulo.procoeton.core.vaadin.view.login.AuthValidators;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author VISTALL
 * @since 27-Sep-16
 */
@PageTitle("Profile")
@PermitAll
@Route("user/info")
public class UserInfoView extends VChildLayout {
    @Autowired
    private BackendUserAccountService myBackendUserAccountService;

    @Override
    public void viewReady(AfterNavigationEvent afterNavigationEvent) {
        removeAll();

        UserAccount accout = SecurityUtil.getUserAccout();
        if (accout == null) {
            return;
        }

        VerticalLayout changePasswordLayout = new VerticalLayout();
        LabeledLayout panel = new LabeledLayout("Change Password", changePasswordLayout);

        //just show
        TextField emailField = new TextField();
        emailField.setReadOnly(true);
        emailField.setAutocomplete(Autocomplete.USERNAME);
        emailField.setValue(accout.getUsername());
        changePasswordLayout.add(VaadinUIUtil.labeledFill("Email", emailField));

        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setAutocomplete(Autocomplete.CURRENT_PASSWORD);
        changePasswordLayout.add(VaadinUIUtil.labeledFill("Old Password", oldPasswordField));
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setAutocomplete(Autocomplete.NEW_PASSWORD);
        changePasswordLayout.add(VaadinUIUtil.labeledFill("New Password", newPasswordField));

        ComponentEventListener<ClickEvent<Button>> listener = clickEvent ->
            changePassword(accout, oldPasswordField, newPasswordField);
        changePasswordLayout.add(new Button("Change Password", listener));

        add(panel);
    }

    private void changePassword(UserAccount account, PasswordField oldPassword, PasswordField newPassword) {
        Binder<ChangePasswordRequest> binder = new Binder<>();
        binder.forField(oldPassword)
            .asRequired()
            .withValidator(AuthValidators.newPasswordValidator())
            .bind(ChangePasswordRequest::getOldPassword, ChangePasswordRequest::setOldPassword);
        binder.forField(newPassword)
            .asRequired()
            .withValidator(AuthValidators.newPasswordValidator())
            .bind(ChangePasswordRequest::getNewPassword, ChangePasswordRequest::setNewPassword);

        ChangePasswordRequest request = new ChangePasswordRequest();
        try {
            binder.writeBean(request);
        }
        catch (ValidationException ignored) {
            return;
        }

        if (!myBackendUserAccountService.changePassword(account.getId(), request.getOldPassword(), request.getNewPassword())) {
            Notifications.error("Failed to change password");
            return;
        }

        Notifications.info("Password changed");
    }
}
