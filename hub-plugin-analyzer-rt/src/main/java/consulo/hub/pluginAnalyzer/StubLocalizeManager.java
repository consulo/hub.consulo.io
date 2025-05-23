package consulo.hub.pluginAnalyzer;

import consulo.disposer.Disposable;
import consulo.localize.LocalizeKey;
import consulo.localize.LocalizeManager;
import consulo.localize.LocalizeManagerListener;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class StubLocalizeManager extends LocalizeManager {
    @Nonnull
    @Override
    public LocalizeValue fromStringKey(@Nonnull String s) {
        return LocalizeValue.of(s);
    }

    @Nonnull
    @Override
    public Map.Entry<Locale, String> getUnformattedText(@Nonnull LocalizeKey localizeKey) {
        return Map.entry(getLocale(), "");
    }

    @Nonnull
    @Override
    public Locale parseLocale(@Nonnull String s) {
        return getLocale();
    }

    @Override
    public void setLocale(@Nonnull Locale locale, boolean b) {

    }

    @Nonnull
    @Override
    public Locale getLocale() {
        return Locale.US;
    }

    @Nonnull
    @Override
    public Locale getAutoDetectedLocale() {
        return getLocale();
    }

    @Override
    public boolean isDefaultLocale() {
        return true;
    }

    @Nonnull
    @Override
    public Set<Locale> getAvaliableLocales() {
        return Set.of();
    }

    @Override
    public void addListener(@Nonnull LocalizeManagerListener localizeManagerListener, @Nonnull Disposable disposable) {

    }

    @Override
    public long getModificationCount() {
        return 0;
    }

    @Nonnull
    @Override
    public String formatText(String s, Locale locale, Object[] objects) {
        return s;
    }
}
