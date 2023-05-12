package consulo.hub.backend.frontend;

import consulo.hub.shared.ServicesHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 12/05/2023
 */
@Component
public class FrontendSecureKeyCheckerFilter extends OncePerRequestFilter
{
	private final AntPathRequestMatcher myMatcher;
	private final String mySecureKey;

	public FrontendSecureKeyCheckerFilter(@Value("${backend.secure.key:}") String secureKey)
	{
		mySecureKey = secureKey;
		// see BackendSecurity
		myMatcher = new AntPathRequestMatcher("/api/private/**");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
	{
		if(!myMatcher.matches(request))
		{
			filterChain.doFilter(request, response);
			return;
		}

		String headerValue = request.getHeader(ServicesHeaders.BACKEND_SECURE_KEY);

		if(!Objects.equals(headerValue, mySecureKey))
		{
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Backend Secure Key Check Failed");
		}
		else
		{
			filterChain.doFilter(request, response);
		}
	}
}
