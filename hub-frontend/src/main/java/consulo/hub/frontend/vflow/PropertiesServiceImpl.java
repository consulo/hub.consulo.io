package consulo.hub.frontend.vflow;

import com.google.common.annotations.VisibleForTesting;
import consulo.procoeton.core.ProPropertiesService;
import consulo.procoeton.core.util.PropertySet;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 2016-08-28
 */
@Service
public class PropertiesServiceImpl implements ProPropertiesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesServiceImpl.class);

    private final File myConfigDirectory;

    private PropertySet myPropertySet;

    @Autowired
    public PropertiesServiceImpl() {
        this(null);
    }

    @VisibleForTesting
    public PropertiesServiceImpl(String home) {
        myConfigDirectory = home == null ? new File(".hub-frontend") : new File(home, ".hub-frontend");

        myConfigDirectory.mkdirs();
    }

    @Nonnull
    @Override
    public PropertySet getPropertySet() {
        return Objects.requireNonNull(myPropertySet);
    }

    public boolean isNotInstalled() {
        return myPropertySet == null;
    }

    @Override
    public boolean isInstalled() {
        return myPropertySet != null;
    }

    public void resetProperties() {
        myPropertySet = null;
    }

    public void setProperties(Properties properties) {
        File file = new File(myConfigDirectory, "config.xml");
        FileSystemUtils.deleteRecursively(file);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            properties.storeToXML(fileOutputStream, "hub.consulo.io");

            reloadProperties();
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void reloadProperties() {
        File file = new File(myConfigDirectory, "config.xml");
        if (file.exists()) {
            Properties properties = new Properties();
            try {
                try (FileInputStream in = new FileInputStream(file)) {
                    properties.loadFromXML(in);
                }
                myPropertySet = new PropertySet(properties);
            }
            catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @PostConstruct
    public void contextInitialized() {
        reloadProperties();
    }
}
