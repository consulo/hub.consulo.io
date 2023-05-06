package consulo.hub.pluginAnalyzer;

import consulo.disposer.Disposable;
import consulo.localize.LocalizeKey;
import consulo.localize.LocalizeManager;
import consulo.localize.LocalizeManagerListener;
import consulo.localize.LocalizeValue;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Set;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class StubLocalizeManager extends LocalizeManager
{
	@Nonnull
	@Override
	public LocalizeValue fromStringKey(@Nonnull String s)
	{
		return LocalizeValue.of(s);
	}

	@Nonnull
	@Override
	public String getUnformattedText(@Nonnull LocalizeKey localizeKey)
	{
		return localizeKey.getKey();
	}

	@Nonnull
	@Override
	public Locale parseLocale(@Nonnull String s)
	{
		return Locale.ROOT;
	}

	@Override
	public void setLocale(@Nonnull Locale locale, boolean b)
	{

	}

	@Nonnull
	@Override
	public Locale getLocale()
	{
		return Locale.ROOT;
	}

	@Override
	public boolean isDefaultLocale()
	{
		return true;
	}

	@Nonnull
	@Override
	public Set<Locale> getAvaliableLocales()
	{
		return Set.of();
	}

	@Override
	public void addListener(@Nonnull LocalizeManagerListener localizeManagerListener, @Nonnull Disposable disposable)
	{

	}

	@Override
	public long getModificationCount()
	{
		return 0;
	}

	@Nonnull
	@Override
	public String formatText(String s, Object... objects)
	{
		return s;
	}
}
