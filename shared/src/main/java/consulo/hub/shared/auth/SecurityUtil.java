package consulo.hub.shared.auth;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil
{
	private SecurityUtil()
	{
	}

	public static UserAccount getUserAccout()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return null;
		}

		Object principal = authentication.getPrincipal();
		if(!(principal instanceof UserAccount))
		{
			return null;
		}
		return (UserAccount) principal;
	}

	public static boolean isLoggedIn()
	{
		return hasRole(Roles.ROLE_USER);
	}

	public static boolean hasRole(String role)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority(role));
	}
}
