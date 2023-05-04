package consulo.hub.backend.auth;

import consulo.hub.shared.auth.domain.UserAccount;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.annotation.Annotation;

/**
 * @author VISTALL
 * @see org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
 * @since 04/05/2023
 */
public class UserAccountHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver
{
	private final UserAccountService myUserAccountService;

	public UserAccountHandlerMethodArgumentResolver(UserAccountService userAccountService)
	{
		myUserAccountService = userAccountService;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		return findMethodAnnotation(AuthenticationPrincipal.class, parameter) != null;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception
	{
		SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();
		Authentication authentication = strategy.getContext().getAuthentication();
		if(authentication == null)
		{
			return null;
		}

		Object principal = authentication.getPrincipal();
		if(principal == null)
		{
			return null;
		}

		if(principal instanceof Jwt jwt)
		{
			String principalClaimValue = jwt.getClaimAsString(JwtClaimNames.SUB);

			UserAccount account = myUserAccountService.findUser(principalClaimValue);
			if(account != null)
			{
				return account;
			}
		}
		return null;
	}

	private <T extends Annotation> T findMethodAnnotation(Class<T> annotationClass, MethodParameter parameter)
	{
		T annotation = parameter.getParameterAnnotation(annotationClass);
		if(annotation != null)
		{
			return annotation;
		}
		Annotation[] annotationsToSearch = parameter.getParameterAnnotations();
		for(Annotation toSearch : annotationsToSearch)
		{
			annotation = AnnotationUtils.findAnnotation(toSearch.annotationType(), annotationClass);
			if(annotation != null)
			{
				return annotation;
			}
		}
		return null;
	}
}
