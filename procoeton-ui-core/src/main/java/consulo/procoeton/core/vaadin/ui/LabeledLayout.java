package consulo.procoeton.core.vaadin.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import consulo.procoeton.core.vaadin.util.ProcoetonStyles;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
public class LabeledLayout extends VerticalLayout {
    public LabeledLayout(String caption, Component component) {
        setMargin(false);
        Span span = new Span(caption);
        span.addClassName(ProcoetonStyles.FontWeight.BOLD);
        span.setWidthFull();
        add(span);
        add(component);

        if (component instanceof ThemableLayout themableLayout) {
            themableLayout.setMargin(false);
            themableLayout.setPadding(false);
        }

        addClassName(ProcoetonStyles.Border.SOLID);
        addClassName(ProcoetonStyles.BorderRadius.SMALL);
        addClassName(ProcoetonStyles.BorderColor.BASE);
    }
}
