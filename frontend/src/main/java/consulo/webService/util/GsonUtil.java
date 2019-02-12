package consulo.webService.util;

import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
public class GsonUtil
{
	private static final Gson ourGson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).create();

	private static final Gson ourPrettyGson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).create();

	@Nonnull
	public static Gson prettyGet()
	{
		return ourPrettyGson;
	}

	@Nonnull
	public static Gson get()
	{
		return ourGson;
	}
}
