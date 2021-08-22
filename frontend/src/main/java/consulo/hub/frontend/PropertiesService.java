package consulo.hub.frontend;

import com.google.common.annotations.VisibleForTesting;
import consulo.hub.frontend.util.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 28-Aug-16
 */
@Service
public class PropertiesService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesService.class);

	private final File myConfigDirectory;

	private PropertySet myPropertySet;

	@Autowired
	public PropertiesService()
	{
		this(System.getProperty("user.home"));
	}

	@VisibleForTesting
	public PropertiesService(String userHome)
	{
		myConfigDirectory = new File(userHome, ".consuloWebservice");

		myConfigDirectory.mkdirs();
	}

	public PropertySet getPropertySet()
	{
		return Objects.requireNonNull(myPropertySet);
	}

	public boolean isNotInstalled()
	{
		return myPropertySet == null;
	}

	public void setProperties(Properties properties)
	{
		File file = new File(myConfigDirectory, "config.xml");
		FileSystemUtils.deleteRecursively(file);

		try (FileOutputStream fileOutputStream = new FileOutputStream(file))
		{
			properties.storeToXML(fileOutputStream, "hub.consulo.io");

			reloadProperties();
		}
		catch(IOException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void reloadProperties()
	{
		File file = new File(myConfigDirectory, "config.xml");
		if(file.exists())
		{
			Properties properties = new Properties();
			try
			{
				try (FileInputStream in = new FileInputStream(file))
				{
					properties.loadFromXML(in);
				}
				myPropertySet = new PropertySet(properties);
			}
			catch(Exception e)
			{
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	@PostConstruct
	public void contextInitialized()
	{
		reloadProperties();
	}
}
