package consulo.hub.pluginAnalyzer;

import consulo.localize.LocalizeValue;
import consulo.ui.*;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.color.ColorValue;
import consulo.ui.font.FontManager;
import consulo.ui.image.Image;
import consulo.ui.image.ImageKey;
import consulo.ui.image.canvas.Canvas2D;
import consulo.ui.internal.UIInternal;
import consulo.ui.layout.*;
import consulo.ui.model.ListModel;
import consulo.ui.model.MutableListModel;
import consulo.ui.style.StyleManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2019-07-06
 */
public class StubUIInternal extends UIInternal
{
	private static final Image stubImage = new Image()
	{
		@Override
		public int getHeight()
		{
			return 0;
		}

		@Override
		public int getWidth()
		{
			return 0;
		}
	};

	private static class StubImageKey implements ImageKey
	{
		private final String myGroupId;
		private final String myImageId;
		private final int myWidth;
		private final int myHeight;

		public StubImageKey(String groupId, String imageId, int width, int height)
		{
			myGroupId = groupId;
			myImageId = imageId;
			myWidth = width;
			myHeight = height;
		}

		@Nonnull
		@Override
		public String getGroupId()
		{
			return myGroupId;
		}

		@Nonnull
		@Override
		public String getImageId()
		{
			return myImageId;
		}

		@Override
		public int getHeight()
		{
			return myHeight;
		}

		@Override
		public int getWidth()
		{
			return myWidth;
		}
	}

	@Override
	public ImageKey _ImageKey_of(@Nonnull String groupId, @Nonnull String imageId, int width, int height)
	{
		return new StubImageKey(groupId, imageId, width, height);
	}

	@Override
	public CheckBox _Components_checkBox()
	{
		return null;
	}

	@Override
	public DockLayout _Layouts_dock(int gap)
	{
		return null;
	}

	@Override
	public WrappedLayout _Layouts_wrapped()
	{
		return null;
	}

	@Override
	public VerticalLayout _Layouts_vertical(int gap)
	{
		return null;
	}

	@Override
	public SwipeLayout _Layouts_swipe()
	{
		return null;
	}

	@Override
	public TwoComponentSplitLayout _TwoComponentSplitLayout_create(SplitLayoutPosition splitLayoutPosition)
	{
		return null;
	}

	@Override
	public ThreeComponentSplitLayout _ThreeComponentSplitLayout_create(SplitLayoutPosition splitLayoutPosition)
	{
		return null;
	}

	@Override
	public TabbedLayout _Layouts_tabbed()
	{
		return null;
	}

	@Override
	public LabeledLayout _Layouts_labeled(LocalizeValue localizeValue)
	{
		return null;
	}

	@Override
	public HorizontalLayout _Layouts_horizontal(int i)
	{
		return null;
	}

	@Override
	public Label _Components_label(LocalizeValue s, LabelOptions options)
	{
		return null;
	}

	@Override
	public HtmlLabel _Components_htmlLabel(LocalizeValue s, LabelOptions options)
	{
		return null;
	}

	@Override
	public <E> ComboBox<E> _Components_comboBox(ListModel<E> listModel)
	{
		return null;
	}

	@Override
	public TextBox _Components_textBox(String s)
	{
		return null;
	}

	@Override
	public ProgressBar _Components_progressBar()
	{
		return null;
	}

	@Nonnull
	@Override
	public IntBox _Components_intBox(int i)
	{
		return null;
	}

	@Override
	public <E> ListBox<E> _Components_listBox(ListModel<E> listModel)
	{
		return null;
	}

	@Override
	public Hyperlink _Components_hyperlink(String s)
	{
		return null;
	}

	@Override
	public ImageBox _Components_imageBox(Image image)
	{
		return null;
	}

	@Override
	public ColorBox _Components_colorBox(@Nullable ColorValue colorValue)
	{
		return null;
	}

	@Override
	public Image _Image_fromUrl(URL url)
	{
		return stubImage;
	}

	@Override
	public Image _Image_fromStream(Image.ImageType imageType, InputStream stream) throws IOException
	{
		return stubImage;
	}

	@Override
	public Image _Image_lazy(Supplier<Image> supplier)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_layered(Image[] images)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_transparent(@Nonnull Image image, float v)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_grayed(@Nonnull Image image)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_appendRight(@Nonnull Image image, @Nonnull Image image1)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_empty(int i, int i1)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_canvas(int i, int i1, Consumer<Canvas2D> consumer)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_withText(Image image, String s)
	{
		return stubImage;
	}

	@Override
	public Image _ImageEffects_resize(Image image, int i, int i1)
	{
		return stubImage;
	}

	@Override
	public MenuItem _MenuItem_create(String s)
	{
		return null;
	}

	@Override
	public Menu _Menu_create(String s)
	{
		return null;
	}

	@Override
	public MenuSeparator _MenuSeparator_create()
	{
		return null;
	}

	@Override
	public ValueGroup<Boolean> _ValueGroups_boolGroup()
	{
		return null;
	}

	@Override
	public MenuBar _MenuItems_menuBar()
	{
		return null;
	}

	@Nonnull
	@Override
	public StyleManager _StyleManager_get()
	{
		return null;
	}

	@Nonnull
	@Override
	public FontManager _FontManager_get()
	{
		return null;
	}

	@Nonnull
	@Override
	public Window _Window_create(String s, WindowOptions windowOptions)
	{
		return null;
	}

	@Nullable
	@Override
	public Window _Window_getActiveWindow()
	{
		return null;
	}

	@Nullable
	@Override
	public Window _Window_getFocusedWindow()
	{
		return null;
	}

	@Override
	public <T> Alert<T> _Alerts_create()
	{
		return null;
	}

	@Override
	public <T> ListModel<T> _ListModel_create(Collection<? extends T> collection)
	{
		return null;
	}

	@Override
	public <T> MutableListModel<T> _MutableListModel_create(Collection<? extends T> collection)
	{
		return null;
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	public UIAccess _UIAccess_get()
	{
		return null;
	}

	@Override
	public boolean _UIAccess_isUIThread()
	{
		return false;
	}

	@Override
	public TextBoxWithExpandAction _Components_textBoxWithExpandAction(Image image, String s, Function<String, List<String>> function, Function<List<String>, String> function1)
	{
		return null;
	}

	@Override
	public TextBoxWithExtensions _Components_textBoxWithExtensions(String s)
	{
		return null;
	}

	@Override
	public FoldoutLayout _Layouts_foldout(LocalizeValue localizeValue, Component component, boolean b)
	{
		return null;
	}
}
