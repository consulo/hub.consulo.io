package consulo.hub.pluginAnalyzer.logger;

import consulo.logging.Logger;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06/07/2023
 */
public class SilentLogger implements Logger {
    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String s) {
        System.out.println(s);
    }

    @Override
    public void debug(@Nullable Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void debug(String s, @Nullable Throwable throwable) {
        System.out.println(s);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void info(String s) {
        System.out.println(s);
    }

    @Override
    public void info(String s, @Nullable Throwable throwable) {
        System.out.println(s);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void warn(String s, @Nullable Throwable throwable) {
        System.out.println(s);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void error(String message, @Nullable Throwable t, String... details) {
        System.out.println(message);
        if (t != null) {
            t.printStackTrace();
        }
    }
}
