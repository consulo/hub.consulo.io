package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.badge.BadgeVariant;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class Badge extends com.vaadin.flow.component.badge.Badge {
    public Badge(String text, BadgeVariant... variants) {
        super(text);
        if (variants.length > 0) {
            addThemeVariants(variants);
        }
    }
}
