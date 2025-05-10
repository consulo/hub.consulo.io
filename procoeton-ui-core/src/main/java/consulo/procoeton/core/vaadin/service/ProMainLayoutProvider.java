package consulo.procoeton.core.vaadin.service;

import com.vaadin.flow.router.RouterLayout;
import org.springframework.lang.NonNull;

/**
 * @author VISTALL
 * @since 04/05/2023
 */
public interface ProMainLayoutProvider {
    @NonNull
    Class<? extends RouterLayout> getLayoutClass();
}
