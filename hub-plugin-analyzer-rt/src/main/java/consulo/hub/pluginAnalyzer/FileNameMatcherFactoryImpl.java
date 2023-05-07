package consulo.hub.pluginAnalyzer;

import consulo.virtualFileSystem.fileType.FileNameMatcher;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.internal.matcher.ExactFileNameMatcher;
import consulo.virtualFileSystem.internal.matcher.ExtensionFileNameMatcher;
import consulo.virtualFileSystem.internal.matcher.WildcardFileNameMatcher;

import javax.annotation.Nonnull;

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
		return new ExtensionFileNameMatcher(extension);
	}

	@Nonnull
	@Override
	public FileNameMatcher createExactFileNameMatcher(@Nonnull String fileName, boolean ignoreCase)
	{
		return new ExactFileNameMatcher(fileName, ignoreCase);
	}

	@Nonnull
	@Override
	public FileNameMatcher createWildcardFileNameMatcher(@Nonnull String pattern)
	{
		return new WildcardFileNameMatcher(pattern);
	}
}
