package consulo.hub.frontend.vflow;

import com.vaadin.flow.router.Route;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.procoeton.core.vaadin.ui.VChildLayout;
import jakarta.annotation.security.PermitAll;

/**
 * @author VISTALL
 * @since 30/04/2023
 */
@Route(value = "/stub", layout = MainLayout.class)
@PermitAll
public class StubView extends VChildLayout
{
}
