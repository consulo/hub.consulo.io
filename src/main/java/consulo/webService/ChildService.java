package consulo.webService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
public abstract class ChildService implements ServletContextListener
{
	private boolean myInitialized;

	protected abstract void contextInitializedImpl(ServletContextEvent servletContextEvent);

	public String getTitle()
	{
		return "Update Service";
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent)
	{
		contextInitializedImpl(servletContextEvent);

		myInitialized = true;
	}

	public boolean isInitialized()
	{
		return myInitialized;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
	}
}
