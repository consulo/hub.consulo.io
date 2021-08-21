package consulo.hub.frontend.util;

import java.util.Properties;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
public class PropertySet
{
	private Properties myProperties;

	public PropertySet(Properties properties)
	{
		myProperties = properties;
	}

	public String getStringProperty(String name)
	{
		return myProperties.getProperty(name);
	}

	public boolean getBoolProperty(String name)
	{
		String property = myProperties.getProperty(name);
		return Boolean.valueOf(property);
	}
}
