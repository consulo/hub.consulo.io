package consulo.hub.backend.github.release;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public interface GithubTagBuilder
{
	String buildTagName();

	String buildReleaseName();
}
