package consulo.pluginAnalyzer;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;

import com.intellij.util.ui.JBUI;
import consulo.ui.migration.IconLoaderFacade;
import consulo.ui.migration.SwingImageRef;

/**
 * @author VISTALL
 * @since 2019-04-25
 */
public class StubIconLoaderFacade implements IconLoaderFacade
{
	private static class StubImage implements SwingImageRef
	{
		private static final StubImage INSTANCE = new StubImage();

		@Override
		public int getHeight()
		{
			return 16;
		}

		@Override
		public int getWidth()
		{
			return 16;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y)
		{

		}

		@Override
		public int getIconWidth()
		{
			return 16;
		}

		@Override
		public int getIconHeight()
		{
			return 16;
		}
	}

	@Override
	public void activate()
	{

	}

	@Override
	public void resetDark()
	{

	}

	@Override
	public Icon getDisabledIcon(@Nullable Icon icon)
	{
		return StubImage.INSTANCE;
	}

	@Override
	public Icon getIconSnapshot(@Nonnull Icon icon)
	{
		return icon;
	}

	@Override
	public SwingImageRef findIcon(URL url, boolean b)
	{
		return StubImage.INSTANCE;
	}

	@Override
	public void set(Icon icon, String s, ClassLoader classLoader)
	{
	}

	@Override
	public Image toImage(Icon icon, @Nullable JBUI.ScaleContext scaleContext)
	{
		return null;
	}
}
