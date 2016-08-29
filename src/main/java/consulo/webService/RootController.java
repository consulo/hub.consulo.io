package consulo.webService;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.SystemProperties;
import consulo.webService.update.UpdateService;
import consulo.webService.util.ConsuloHelper;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@WebListener
public class RootController implements ServletContextListener
{
	private static RootController ourInstance;
	private static boolean ourInitialized;

	@NotNull
	public static RootController getInstance() throws ServiceIsNotReadyException
	{
		if(!ourInitialized)
		{
			throw new ServiceIsNotReadyException();
		}

		return getInstanceNoState();
	}

	@NotNull
	public static RootController getInstanceNoState()
	{
		return ourInstance;
	}

	private ChildService[] myChildServices = new ChildService[]{
			new UpdateService()
	};

	private File myConsuloWebServiceHome;

	public ChildService[] getChildServices()
	{
		return myChildServices;
	}

	@NotNull
	public UpdateService getUpdateService()
	{
		return (UpdateService) myChildServices[0];
	}

	@NotNull
	public File getConsuloWebServiceHome()
	{
		return myConsuloWebServiceHome;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent)
	{
		ourInstance = this;

		ConsuloHelper.init();

		String userHome = SystemProperties.getUserHome();

		myConsuloWebServiceHome = new File(userHome, ".consuloWebservice");
		FileUtil.createDirectory(myConsuloWebServiceHome);

		for(ChildService contextListener : myChildServices)
		{
			contextListener.contextInitialized(servletContextEvent);
		}

		ourInitialized = true;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		for(ChildService contextListener : myChildServices)
		{
			contextListener.contextDestroyed(servletContextEvent);
		}
	}
}
