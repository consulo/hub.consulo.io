package consulo.pluginAnalyzer;

import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author VISTALL
 * @since 20-Sep-16
 */
public class CollectFileTypeConsumer implements FileTypeConsumer
{
	@Nonnull
	private final Set<String> myExtensions;
	private final Set<String> myExtensionsV2;

	public CollectFileTypeConsumer(@Nonnull Set<String> extensions, @Nonnull Set<String> extensionsV2)
	{
		myExtensions = extensions;
		myExtensionsV2 = extensionsV2;
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
			processExtensionsOld(fileNameMatcher);

			processExtensionsV2(fileNameMatcher);
		}
	}

	private void processExtensionsOld(FileNameMatcher fileNameMatcher)
	{
		// we accept only our file matches
		if(fileNameMatcher instanceof ExactFileNameMatcher || fileNameMatcher instanceof ExtensionFileNameMatcher || fileNameMatcher instanceof WildcardFileNameMatcher)
		{
			myExtensions.add(fileNameMatcher.getPresentableString());
		}
	}

	private void processExtensionsV2(@Nonnull FileNameMatcher fileNameMatcher)
	{
		String id = buildMatcherIdentificator(fileNameMatcher);
		if(id == null)
		{
			return;
		}

		myExtensionsV2.add(id);
	}

	@Nullable
	private String buildMatcherIdentificator(FileNameMatcher fileNameMatcher)
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
	public FileType getStandardFileTypeByName(@NonNls @Nonnull String type)
	{
		return null;
	}
}
