package consulo.app.plugins.frontend.ui.pluginView;

import com.google.gson.Gson;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import org.apache.commons.lang3.StringUtils;

/**
 * @author VISTALL
 * @since 2025-05-13
 */
@JavaScript("./consuloConnector.js")
public class InstallOrDownloadButtonPanel extends Div {
    public InstallOrDownloadButtonPanel() {
        getStyle().set("alignContent", "center");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        getElement().executeJs("connectToConsulo($0)", getElement());
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
                add(new Button("Install to Consulo #" + response.data.build, event -> {
                    Notification notification = new Notification("No implemented for now", 5000, Notification.Position.TOP_END);
                    notification.open();
                }));
            }
        }
        catch (Exception ignored) {
        }
    }
}
