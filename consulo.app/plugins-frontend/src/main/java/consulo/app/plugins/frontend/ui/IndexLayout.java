package consulo.app.plugins.frontend.ui;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoIcon;
import consulo.app.plugins.frontend.backend.service.BackendRepositoryService;
import consulo.procoeton.core.vaadin.ui.VChildLayout;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
@Route(value = "/", layout = PluginsAppLayout.class)
@AnonymousAllowed
public class IndexLayout extends VChildLayout {
    private final BackendRepositoryService myBackendRepositoryService;

    public IndexLayout(BackendRepositoryService backendRepositoryService) {
        myBackendRepositoryService = backendRepositoryService;

        add(new Span("Test2"));

        Div div = new Div();
        div.addClassName("plugins-card-holder");

        myBackendRepositoryService.listAll(frontPluginNode -> System.out.println(frontPluginNode));

        for (int i = 0; i < 100; i++) {
            Card pluginCard = new Card();
            pluginCard.addThemeVariants(CardVariant.LUMO_COVER_MEDIA, CardVariant.LUMO_HORIZONTAL, CardVariant.LUMO_ELEVATED);
            pluginCard.setHeight(12, Unit.EM);
            pluginCard.setMaxHeight(12, Unit.EM);
            pluginCard.setMaxWidth(35, Unit.EM);

            Icon icon = LumoIcon.PHOTO.create();
            icon.getStyle()
                .setColor("var(--lumo-primary-color)")
                .setBackgroundColor("var(--lumo-primary-color-10pct)");
            pluginCard.setMedia(icon);

            pluginCard.setTitle(new Div("Lapland"));
            pluginCard.setSubtitle(new Div("The Exotic North"));
            pluginCard.add("Lapland is the northern-most region of Finland and an active outdoor destination.");
            
            div.add(pluginCard);
        }

        add(div);
    }
}
