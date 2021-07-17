package consulo.webService.util;

import com.intellij.openapi.util.io.FileUtil;
import consulo.util.lang.ObjectUtil;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author VISTALL
 * @since 17/07/2021
 */
public class ZipUtil
{
	public static void extract(final @Nonnull ZipFile zipFile, @Nonnull File outputDir) throws IOException
	{
		final Enumeration entries = zipFile.entries();
		while(entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry) entries.nextElement();

			extractEntry(entry, zipFile.getInputStream(entry), outputDir);
		}
	}

	public static void extractEntry(ZipEntry entry, final InputStream inputStream, File outputDir) throws IOException
	{
		final boolean isDirectory = entry.isDirectory();
		final String relativeName = entry.getName();
		final File file = new File(outputDir, relativeName);

		FileUtil.createParentDirs(file);
		if(isDirectory)
		{
			file.mkdir();
		}
		else
		{
			try (BufferedInputStream is = new BufferedInputStream(inputStream); BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
			{
				FileUtil.copy(is, os);
			}

			BasicFileAttributeView view = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class);
			view.setTimes(ObjectUtil.notNull(entry.getLastModifiedTime(), entry.getLastModifiedTime()), entry.getLastAccessTime(), ObjectUtil.notNull(entry.getCreationTime(), entry
					.getLastModifiedTime()));
		}
	}
}
