package consulo.procoeton.core.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2025-05-11
 */
public class ThemeUtil {
    public static void notifyUpdate() {
        UI ui = UI.getCurrent();

        boolean isDark = ui.getElement().getThemeList().contains(Lumo.DARK);

        visitRecursive(ui, c -> {
            if (c instanceof ThemeChangeNotifier themeChangeNotifier) {
                themeChangeNotifier.onThemeChange(isDark);
            }
        });
    }

    public static boolean isDark() {
        return UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK);
    }

    private static void visitRecursive(Component component, Consumer<Component> consumer) {
        consumer.accept(component);

        List<Component> list = component.getChildren().toList();
        for (Component child : list) {
            visitRecursive(child, consumer);
        }
    }
}
