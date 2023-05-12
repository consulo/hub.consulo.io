package consulo.procoeton.core.backend;

/**
 * @author VISTALL
 * @since 12/05/2023
 */
public class BackendServiceDownException extends RuntimeException
{
	public BackendServiceDownException(Exception e)
	{
		super(e);
	}
}
