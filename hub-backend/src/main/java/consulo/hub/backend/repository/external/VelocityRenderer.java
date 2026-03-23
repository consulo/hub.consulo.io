package consulo.hub.backend.repository.external;

import jakarta.annotation.Nonnull;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Thin wrapper around {@link VelocityEngine} for classpath-based template rendering.
 *
 * @author VISTALL
 */
@Service
public class VelocityRenderer {
    private final VelocityEngine myEngine;

    public VelocityRenderer() {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "classpath");
        props.setProperty("resource.loader.classpath.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("parser.space_gobbling", "lines");
        props.setProperty("runtime.log.logsystem.class",
            "org.apache.velocity.runtime.log.NullLogChute");
        myEngine = new VelocityEngine(props);
        myEngine.init();
    }

    @Nonnull
    public String render(@Nonnull String templatePath, @Nonnull Map<String, Object> ctx) {
        VelocityContext context = new VelocityContext(new HashMap<>(ctx));
        Template template = myEngine.getTemplate(templatePath, "UTF-8");
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }
}
