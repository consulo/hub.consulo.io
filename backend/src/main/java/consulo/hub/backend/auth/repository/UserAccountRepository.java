package consulo.hub.backend.auth.repository;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author VISTALL
 * @since 20/08/2021
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer>
{
	UserAccount findByUsername(final String username);
}
