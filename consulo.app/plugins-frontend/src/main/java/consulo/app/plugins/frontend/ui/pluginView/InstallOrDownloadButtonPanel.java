package consulo.app.plugins.frontend.ui.pluginView;

import com.google.gson.Gson;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2025-05-13
 */
@JavaScript("./consuloConnector.js")
public class InstallOrDownloadButtonPanel extends Div {
    private final Supplier<String> myPluginIdSupplier;

    public InstallOrDownloadButtonPanel(Supplier<String> pluginIdSupplier) {
        myPluginIdSupplier = pluginIdSupplier;
        getStyle().set("alignContent", "center");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        getElement().executeJs("connectToConsulo($0, $1)", "http://localhost:62242/api/about", getElement());
    }

    @ClientCallable
    public void handleConsuloResponse(int state, String body) {
        removeAll();

        if (StringUtils.isBlank(body)) {
            return;
        }

        Gson gson = new Gson();
        try {
            ConsuloAboutResponse response = gson.fromJson(body, ConsuloAboutResponse.class);

            if (response.success && response.data != null && "Consulo".equals(response.data.name)) {
                Button button = new Button("Install to Consulo #" + response.data.build);
                button.addSingleClickListener(event -> {
                    String url = "http://localhost:62242/api/plugins/install?pluginId=" + myPluginIdSupplier.get();
                    
                    getElement().executeJs("installPluginToConsulo($0, $1)", url, getElement());

                    button.setEnabled(false);
                });
                button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                add(button);
            }
        }
        catch (Exception ignored) {
        }
    }
}
