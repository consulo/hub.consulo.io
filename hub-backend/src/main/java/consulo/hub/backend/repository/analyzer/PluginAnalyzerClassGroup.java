package consulo.hub.backend.repository.analyzer;

import consulo.application.Application;
import consulo.hub.backend.TempFileService;
import consulo.util.io.StreamUtil;
import consulo.util.io.URLUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author VISTALL
 * @since 06/05/2023
 */
public class PluginAnalyzerClassGroup
{
	private static final Logger LOG = LoggerFactory.getLogger(PluginAnalyzerClassGroup.class);

	private Set<String> myRequiredClasses = new LinkedHashSet<>();

	private List<URL> myClassUrls = List.of();

	public void requireClass(Class<?> clazz)
	{
		requireClass(clazz.getName());
	}

	public void requireClass(String clazzName)
	{
		myRequiredClasses.add(clazzName);
	}

	public void init(TempFileService tempFileService) throws Exception
	{
		URL urlLang = getJarUrlForClass(Application.class);

		if(urlLang.toString().contains("BOOT-INF"))
		{
			myClassUrls = prepareRunningInsideBoot(myRequiredClasses, tempFileService);
		}
		else
		{
			myClassUrls = prepareRunningOutsideBoot(myRequiredClasses);
		}
	}

	public List<URL> getClassUrls()
	{
		return myClassUrls;
	}

	private static List<URL> prepareRunningInsideBoot(Set<String> requiredClasses, TempFileService tempFileService) throws Exception
	{
		File librariesTempDir = tempFileService.createTempDir("boot_extracted_libraries");

		Map<String, ZipFile> zipFileMap = new HashMap<>();

		List<URL> resultUrls = new ArrayList<>();

		for(String requiredClass : requiredClasses)
		{
			Class<?> clazz = Class.forName(requiredClass);

			URL url = getJarUrlForClass(clazz);

			//jar:file:/W:/_github.com/consulo/hub.consulo.io/backend/target/hub-backend-1.0-SNAPSHOT.jar!/BOOT-INF/lib/consulo-core-api-2-SNAPSHOT.jar!/

			String urlString = url.toString();

			if(urlString.endsWith("!/"))
			{
				urlString = urlString.substring(0, urlString.length() - 2);
			}

			int firstSeparator = urlString.indexOf("!/");
			if(firstSeparator == -1)
			{
				throw new IllegalArgumentException("Invalid url: " + urlString);
			}

			// we need start slash
			String bootJarFile = urlString.substring(SystemUtils.IS_OS_WINDOWS ? 10 : 9, firstSeparator);

			String jarEntry = urlString.substring(firstSeparator + 2, urlString.length());

			ZipFile zipFile = zipFileMap.computeIfAbsent(bootJarFile, it -> {
				try
				{
					return new ZipFile(it, StandardCharsets.UTF_8);
				}
				catch(IOException e)
				{
					throw new RuntimeException(e);
				}
			});

			ZipEntry entry = zipFile.getEntry(jarEntry);

			String jarName = jarEntry.substring(jarEntry.lastIndexOf("/") + 1, jarEntry.length());
			File targetJarFile = new File(librariesTempDir, jarName);
			try (InputStream inputStream = zipFile.getInputStream(entry); FileOutputStream stream = new FileOutputStream(targetJarFile))
			{
				StreamUtil.copyStreamContent(inputStream, stream);
			}

			resultUrls.add(targetJarFile.toURI().toURL());
		}

		for(ZipFile file : zipFileMap.values())
		{
			try
			{
				file.close();
			}
			catch(IOException ignored)
			{
			}
		}

		return resultUrls;
	}

	private static List<URL> prepareRunningOutsideBoot(Set<String> requiredClasses)
	{
		List<URL> resultUrls = new ArrayList<>();

		for(String clazzName : requiredClasses)
		{
			try
			{
				Class<?> clazz = Class.forName(clazzName);

				File jarPathForClass = getJarPathForClass(clazz);

				resultUrls.add(jarPathForClass.toURI().toURL());
			}
			catch(ClassNotFoundException | MalformedURLException e)
			{
				LOG.error("Class " + clazzName + " is not found", e);
			}
		}

		return resultUrls;
	}

	@Nonnull
	private static File getJarPathForClass(@Nonnull Class aClass)
	{
		CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
		if(codeSource != null)
		{
			URL location = codeSource.getLocation();
			if(location != null)
			{
				return URLUtil.urlToFile(location);
			}
		}
		throw new IllegalArgumentException("can't find path for class " + aClass.getName());
	}

	@Nonnull
	private static URL getJarUrlForClass(@Nonnull Class aClass)
	{
		CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
		if(codeSource != null)
		{
			URL location = codeSource.getLocation();
			if(location != null)
			{
				return location;
			}
		}
		throw new IllegalArgumentException("can't find path for class " + aClass.getName());
	}
}
