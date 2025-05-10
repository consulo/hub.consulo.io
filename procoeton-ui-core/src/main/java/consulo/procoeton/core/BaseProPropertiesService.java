package consulo.procoeton.core;

import consulo.procoeton.core.util.PropertySet;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author VISTALL
 * @since 2025-05-10
 */
public class BaseProPropertiesService implements ProPropertiesService {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final File myConfigDirectory;

    private PropertySet myPropertySet;

    public BaseProPropertiesService(String home, String configDirName) {
        myConfigDirectory = home == null ? new File(configDirName) : new File(home, configDirName);

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
            LOG.error(e.getMessage(), e);
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
                LOG.error(e.getMessage(), e);
            }
        }
        else {
            setProperties(new Properties());
        }
    }

    @PostConstruct
    public void contextInitialized() {
        reloadProperties();
    }
}
