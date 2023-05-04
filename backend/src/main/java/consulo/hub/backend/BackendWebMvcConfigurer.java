package consulo.hub.backend;

import consulo.hub.backend.auth.UserAccountHandlerMethodArgumentResolver;
import consulo.hub.backend.auth.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
@Configuration
public class BackendWebMvcConfigurer implements WebMvcConfigurer
{
	@Autowired
	private UserAccountService myUserAccountService;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers)
	{
		resolvers.add(new UserAccountHandlerMethodArgumentResolver(myUserAccountService));
	}
}
