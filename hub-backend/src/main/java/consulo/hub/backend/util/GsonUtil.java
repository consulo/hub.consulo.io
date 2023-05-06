package consulo.hub.backend.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
public class GsonUtil
{
	private static final Gson ourGson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).create();

	private static final Gson ourPrettyGson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).create();

	public static Gson prettyGet()
	{
		return ourPrettyGson;
	}

	public static Gson get()
	{
		return ourGson;
	}
}
