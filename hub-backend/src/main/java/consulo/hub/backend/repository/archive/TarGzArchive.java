package consulo.hub.backend.repository.archive;

import consulo.util.io.FileUtil;
import consulo.util.io.StreamUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.util.FileSystemUtils;

import jakarta.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 02-Jan-17
 */
public class TarGzArchive
{
	private final Map<String, TarGzArchiveEntry> myEntries = new LinkedHashMap<>();

	public void extract(@Nonnull File from, @Nonnull File targetDirectory) throws IOException
	{
		FileSystemUtils.deleteRecursively(targetDirectory);
		FileUtil.createDirectory(targetDirectory);

		try (TarArchiveInputStream ais = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from))))
		{
			TarArchiveEntry tempEntry;
			while((tempEntry = (TarArchiveEntry) ais.getNextEntry()) != null)
			{
				String name = tempEntry.getName();
				File targetFile = new File(targetDirectory, name);

				byte flags;
				if(tempEntry.isDirectory())
				{
					flags = TarConstants.LF_DIR;
				}
				else if(tempEntry.isSymbolicLink())
				{
					flags = TarConstants.LF_SYMLINK;
				}
				else if(tempEntry.isLink())
				{
					flags = TarConstants.LF_LINK;
				}
				else
				{
					flags = TarConstants.LF_NORMAL;
				}

				TarGzArchiveEntry value = new TarGzArchiveEntry(name, tempEntry.isDirectory(), tempEntry.getMode(), tempEntry.getLastModifiedDate().getTime(), flags, tempEntry.getLinkName());
				if(tempEntry.isDirectory())
				{
					FileUtil.createDirectory(targetFile);
				}
				else
				{
					FileUtil.createParentDirs(targetFile);

					try (OutputStream stream = new FileOutputStream(targetFile))
					{
						StreamUtil.copyStreamContent(ais, stream);
					}

					value.setExtractedFile(targetFile, tempEntry.getSize());
				}

				myEntries.put(name, value);
			}
		}
	}

	public void create(@Nonnull Path file, @Nonnull String type) throws IOException, ArchiveException
	{
		ArchiveStreamFactory factory = new ArchiveStreamFactory();
		try (OutputStream pathStream = createFileStream(file, type))
		{
			ArchiveOutputStream archiveOutputStream = factory.createArchiveOutputStream(type, pathStream);
			if(archiveOutputStream instanceof TarArchiveOutputStream)
			{
				((TarArchiveOutputStream) archiveOutputStream).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
			}

			for(Map.Entry<String, TarGzArchiveEntry> entry : myEntries.entrySet())
			{
				TarGzArchiveEntry value = entry.getValue();

				ArchiveEntry archiveEntry = createEntry(entry.getKey(), value, type);

				if(!value.isDirectory())
				{
					setEntrySize(value, archiveEntry);

					archiveOutputStream.putArchiveEntry(archiveEntry);

					byte[] extractedData = value.getExtractedData();
					if(extractedData != null)
					{
						try (ByteArrayInputStream fileInputStream = new ByteArrayInputStream(extractedData))
						{
							IOUtils.copy(fileInputStream, archiveOutputStream);
						}
					}
					else
					{
						try (FileInputStream fileInputStream = new FileInputStream(value.getExtractedFile()))
						{
							IOUtils.copy(fileInputStream, archiveOutputStream);
						}
					}

					archiveOutputStream.closeArchiveEntry();
				}
			}

			archiveOutputStream.finish();
		}
	}

	private void setEntrySize(TarGzArchiveEntry value, ArchiveEntry archiveEntry)
	{
		if(archiveEntry instanceof ZipArchiveEntry)
		{
			((ZipArchiveEntry) archiveEntry).setSize(value.getSize());
		}
		else if(archiveEntry instanceof TarArchiveEntry)
		{
			((TarArchiveEntry) archiveEntry).setSize(value.getSize());
		}
		else
		{
			throw new IllegalArgumentException(archiveEntry.getClass().getName());
		}
	}

	@Nonnull
	private ArchiveEntry createEntry(String name, TarGzArchiveEntry entry, String type)
	{
		ArchiveEntry archiveEntry;
		switch(type)
		{
			case ArchiveStreamFactory.TAR:
				TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(name, entry.getFlags());
				String linkName = entry.getLinkName();
				if(linkName != null)
				{
					tarArchiveEntry.setLinkName(linkName);
				}
				tarArchiveEntry.setMode(entry.getMode());
				tarArchiveEntry.setModTime(entry.getTime());

				archiveEntry = tarArchiveEntry;
				break;
			case ArchiveStreamFactory.ZIP:
				ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(name);
				zipArchiveEntry.setTime(entry.getTime());
				archiveEntry = zipArchiveEntry;
				break;
			default:
				throw new IllegalArgumentException(type);
		}
		return archiveEntry;
	}

	public boolean removeEntry(@Nonnull String entryName)
	{
		return myEntries.remove(entryName) != null;
	}

	public void putEntry(@Nonnull String entryName, @Nonnull byte[] data, long lastModified)
	{
		TarGzArchiveEntry entry = new TarGzArchiveEntry(entryName, false, TarArchiveEntry.DEFAULT_FILE_MODE, lastModified, TarArchiveEntry.LF_NORMAL, null);
		entry.setExtractedData(data);
		myEntries.put(entryName, entry);
	}

	@Nonnull
	private static OutputStream createFileStream(Path path, String type) throws IOException
	{
		OutputStream fileOutputStream = Files.newOutputStream(path);
		if(ArchiveStreamFactory.TAR.equals(type))
		{
			return new GzipCompressorOutputStream(fileOutputStream);
		}
		return fileOutputStream;
	}
}
