package consulo.hub.pluginAnalyzer;

import consulo.virtualFileSystem.fileType.FileNameMatcher;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.internal.matcher.ExactFileNameMatcherImpl;
import consulo.virtualFileSystem.internal.matcher.ExtensionFileNameMatcherImpl;
import consulo.virtualFileSystem.internal.matcher.WildcardFileNameMatcherImpl;
import jakarta.annotation.Nonnull;

/**
 * Just copy paste from impl module - we do not expose impl to analyzer
 *
 * @author VISTALL
 * @since 07/05/2023
 */
public class FileNameMatcherFactoryImpl extends FileNameMatcherFactory
{
	@Nonnull
	@Override
	public FileNameMatcher createExtensionFileNameMatcher(@Nonnull String extension)
	{
		return new ExtensionFileNameMatcherImpl(extension);
	}

	@Nonnull
	@Override
	public FileNameMatcher createExactFileNameMatcher(@Nonnull String fileName, boolean ignoreCase)
	{
		return new ExactFileNameMatcherImpl(fileName, ignoreCase);
	}

	@Nonnull
	@Override
	public FileNameMatcher createWildcardFileNameMatcher(@Nonnull String pattern)
	{
		return new WildcardFileNameMatcherImpl(pattern);
	}
}
