package consulo.hub.backend.repository.archive;

import java.io.File;

/**
 * @author VISTALL
 * @since 02-Jan-17
 */
public class ArchiveDataEntry
{
	private String myName;
	private boolean myDirectory;
	private int myMode;
	private long myTime;
	private byte myFlags;
	private String myLinkName;

	private File myExtractedFile;
	private byte[] myExtractedData;

	private long mySize = -1;

	public ArchiveDataEntry(String name, boolean directory, int mode, long time, byte flags, String linkName)
	{
		myName = name;
		myDirectory = directory;
		myMode = mode;
		myTime = time;
		myFlags = flags;
		myLinkName = linkName;
	}

	public byte getFlags()
	{
		return myFlags;
	}

	public String getLinkName()
	{
		return myLinkName;
	}

	public void setExtractedData(byte[] extractedData)
	{
		myExtractedData = extractedData;
		mySize = extractedData.length;
	}

	public void setExtractedFile(File extractedFile, long size)
	{
		myExtractedFile = extractedFile;
		mySize = size;
	}

	public String getName()
	{
		return myName;
	}

	public boolean isDirectory()
	{
		return myDirectory;
	}

	public int getMode()
	{
		return myMode;
	}

	public long getTime()
	{
		return myTime;
	}

	public long getSize()
	{
		return mySize;
	}

	public byte[] getExtractedData()
	{
		return myExtractedData;
	}

	public File getExtractedFile()
	{
		return myExtractedFile;
	}
}
