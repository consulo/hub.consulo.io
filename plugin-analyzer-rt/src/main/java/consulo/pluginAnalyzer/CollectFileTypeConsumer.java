package consulo.pluginAnalyzer;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
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
	public void consume(@NotNull FileType fileType)
	{
		consume(fileType, fileType.getDefaultExtension());
	}

	@Override
	public void consume(@NotNull FileType fileType, @NonNls String extensions)
	{
		if(extensions.isEmpty())
		{
			return;
		}

		List<String> split = StringUtil.split(extensions, EXTENSION_DELIMITER);
		for(String ext : split)
		{
			myExtensions.add(ext.toLowerCase(Locale.US));
		}
	}

	@Override
	public void consume(@NotNull FileType fileType, FileNameMatcher... fileNameMatchers)
	{
	}

	@Nullable
	@Override
	public FileType getStandardFileTypeByName(@NonNls @NotNull String s)
	{
		return null;
	}
}
