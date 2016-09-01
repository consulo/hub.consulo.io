package consulo.webService.update;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public enum UpdateChannel
{
	release, // every month
	beta,    // every week
	alpha,   // every day
	nightly, // every commit

	internal
}
