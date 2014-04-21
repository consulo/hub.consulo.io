package org.mustbe.consulo.war.util;

import java.io.IOException;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 21.04.14
 */
public class ApplicationConfiguration
{
	private static Properties ourProperties;

	public static String getProperty(String str)
	{
		if(ourProperties == null)
		{
			Properties properties = new Properties();
			try
			{
				properties.load(ApplicationConfiguration.class.getResourceAsStream("/application.properties"));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			ourProperties = properties;
		}
		return ourProperties.getProperty(str);
	}
}
