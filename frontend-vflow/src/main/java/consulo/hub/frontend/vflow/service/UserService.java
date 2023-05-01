package consulo.hub.frontend.vflow.service;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public interface UserService
{
	@NonNull
	Optional<UserAccount> getCurrentUser();
}
