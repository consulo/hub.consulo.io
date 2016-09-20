package consulo.webservice;

import java.io.File;
import java.io.InputStream;

import javax.servlet.ServletContextEvent;

import com.intellij.openapi.util.io.FileUtilRt;
import consulo.webService.RootService;
import consulo.webService.update.UpdateChannel;
import consulo.webService.update.servlet.PluginDeploy;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class AnalyzerTest
{
	public static void main(String[] args) throws Exception
	{
		File file = new File("C:\\Users\\VISTALL\\AppData\\Local\\Temp\\webService");
		File[] files = file.listFiles();
		if(files != null)
		{
			for(File child : files)
			{
				FileUtilRt.delete(child);
			}
		}

		String canonicalPath = file.getCanonicalPath();

		System.out.println(canonicalPath);

		RootService rootService = new RootService(canonicalPath);

		rootService.contextInitialized(new ServletContextEvent(DummyServletContext.ourDummyInstance));

		InputStream resourceAsStream = AnalyzerTest.class.getResourceAsStream("/consulo.java_178.zip");

		PluginDeploy.deployPlugin(UpdateChannel.nightly, () -> resourceAsStream);
	}
}
