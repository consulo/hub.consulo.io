package consulo.hub.frontend.vflow.user.view;

/**
 * @author VISTALL
 * @since 2023-05-05
 */
public class ChangePasswordRequest {
    private String myOldPassword;
    private String myNewPassword;

    public String getOldPassword() {
        return myOldPassword;
    }

    public void setOldPassword(String oldPassword) {
        myOldPassword = oldPassword;
    }

    public String getNewPassword() {
        return myNewPassword;
    }

    public void setNewPassword(String newPassword) {
        myNewPassword = newPassword;
    }
}
