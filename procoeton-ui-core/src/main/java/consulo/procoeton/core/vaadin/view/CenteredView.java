package consulo.procoeton.core.vaadin.view;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import consulo.procoeton.core.vaadin.util.ProcoetonStyles;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public abstract class CenteredView extends VChildLayout {
    private VerticalLayout myCenterLayout = new VerticalLayout();

    public CenteredView() {
        myCenterLayout.setPadding(false);
        myCenterLayout.setMargin(false);
        myCenterLayout.addClassName(ProcoetonStyles.Margin.AUTO);
        myCenterLayout.setWidth(20, Unit.EM);
        add(myCenterLayout);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHorizontalComponentAlignment(Alignment.CENTER, myCenterLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        myCenterLayout.removeAll();

        String headerText = getHeaderText();
        if (headerText != null) {
            myCenterLayout.add(new H2(headerText));
        }

        fill(myCenterLayout, event.getLocation());

        super.beforeEnter(event);
    }

    protected abstract void fill(VerticalLayout layout, Location location);

    @Nullable
    protected abstract String getHeaderText();
}
