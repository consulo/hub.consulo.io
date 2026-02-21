package consulo.procoeton.core.vaadin.service;

import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public interface ProMainLayoutProvider {
    @NonNull
    Class<? extends RouterLayout> getLayoutClass();

    @Nullable
    default IndexHtmlRequestListener createIndexHtmlRequestListener() {
        return null;
    }
}
