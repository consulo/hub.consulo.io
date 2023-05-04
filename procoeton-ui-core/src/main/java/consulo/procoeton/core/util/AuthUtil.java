package consulo.procoeton.core.util;

import consulo.hub.shared.auth.SecurityUtil;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 21/08/2021
 */
public class AuthUtil
{
	public static long getUserId()
	{
		return Objects.requireNonNull(SecurityUtil.getUserAccout()).getId();
	}
}
