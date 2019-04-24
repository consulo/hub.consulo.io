package consulo.pluginAnalyzer;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NonNls;
import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.WildcardFileNameMatcher;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class CollectFileTypeConsumer implements FileTypeConsumer
{
	private Set<String> myExtensions;

	public CollectFileTypeConsumer(Set<String> extensions)
	{
		myExtensions = extensions;
	}

	@Override
	public void consume(@Nonnull FileType fileType)
	{
		consume(fileType, fileType.getDefaultExtension());
	}

	@Override
	public void consume(@Nonnull FileType fileType, @NonNls String extensions)
	{
		if(extensions.isEmpty())
		{
			return;
		}

		List<String> split = StringUtil.split(extensions, EXTENSION_DELIMITER);
		for(String ext : split)
		{
			consume(fileType, new ExtensionFileNameMatcher(ext.toLowerCase(Locale.US)));
		}
	}

	@Override
	public void consume(@Nonnull FileType fileType, FileNameMatcher... fileNameMatchers)
	{
		if(fileType == PlainTextFileType.INSTANCE)
		{
			return;
		}

		for(FileNameMatcher fileNameMatcher : fileNameMatchers)
		{
			String text = getText(fileNameMatcher);
			if(text == null)
			{
				continue;
			}

			myExtensions.add(text);
		}
	}

	@Nullable
	private String getText(FileNameMatcher fileNameMatcher)
	{
		if(fileNameMatcher instanceof ExactFileNameMatcher)
		{
			if(((ExactFileNameMatcher) fileNameMatcher).isIgnoreCase())
			{
				return "!|" + ((ExactFileNameMatcher) fileNameMatcher).getFileName();
			}
			else
			{
				return "¡|" + ((ExactFileNameMatcher) fileNameMatcher).getFileName();
			}
		}
		else if(fileNameMatcher instanceof ExtensionFileNameMatcher)
		{
			return "*|" + ((ExtensionFileNameMatcher) fileNameMatcher).getExtension();
		}
		else if(fileNameMatcher instanceof WildcardFileNameMatcher)
		{
			return "?|" + ((WildcardFileNameMatcher) fileNameMatcher).getPattern();
		}
		return null;
	}

	@Nullable
	@Override
	public FileType getStandardFileTypeByName(@NonNls @Nonnull String s)
	{
		return null;
	}
}
