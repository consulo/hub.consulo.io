package consulo.hub.backend.util;

/**
 * @author VISTALL
 * @since 29/04/2023
 */
public abstract class AccessToken implements AutoCloseable
{
	public static final AccessToken EMPTY_ACCESS_TOKEN = new AccessToken()
	{
		@Override
		public void finish()
		{

		}
	};

	public abstract void finish();

	@Override
	public void close()
	{
		finish();
	}
}
