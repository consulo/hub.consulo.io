package consulo.hub.frontend.storage.view;

import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.io.UnsyncByteArrayInputStream;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import consulo.hub.frontend.backend.service.StorageService;
import consulo.hub.frontend.base.ui.util.TinyComponents;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;
import consulo.hub.frontend.util.AuthUtil;
import consulo.hub.shared.base.InformationBean;
import consulo.hub.shared.storage.domain.StorageFile;
import consulo.hub.shared.storage.domain.StorageFileUpdateBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author VISTALL
 * @since 19-Feb-17
 */
@SpringView(name = StorageView.ID)
public class StorageView extends VerticalLayout implements View
{
	public static final String ID = "storage";

	@Autowired
	private StorageService myStorageService;

	public StorageView()
	{
		setMargin(false);
		setSpacing(false);
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event)
	{
		removeAllComponents();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return;
		}

		ListSelect<String> listSelect = TinyComponents.newListSelect();
		VerticalLayout updateInfoPanel = new VerticalLayout();

		List<StorageFile> files = myStorageService.findAllByUser(AuthUtil.getUserId());

		Label label = new Label("Storage: ");

		Button wipeDataButton = TinyComponents.newButton("Wipe All", e -> {
			myStorageService.wipeData(AuthUtil.getUserId());

			listSelect.setDataProvider(new ListDataProvider<>(new ArrayList<>()));
			listSelect.setItemCaptionGenerator(s -> "");
			updateInfoPanel.removeAllComponents();
		});

		HorizontalLayout header = VaadinUIUtil.newHorizontalLayout();
		header.setWidth(100, Unit.PERCENTAGE);
		header.addStyleName("headerMargin");
		header.addComponent(label);
		header.addComponent(wipeDataButton);

		header.setComponentAlignment(wipeDataButton, Alignment.MIDDLE_RIGHT);
		wipeDataButton.addStyleName(ValoTheme.BUTTON_DANGER);

		addComponent(header);

		HorizontalSplitPanel panel = new HorizontalSplitPanel();

		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.setMargin(new MarginInfo(false, true, false, true));
		rightLayout.setSpacing(false);
		rightLayout.setSizeFull();

		listSelect.setSizeFull();

		TextArea textArea = TinyComponents.newTextArea();
		textArea.setCaption("Text: ");
		textArea.setSizeFull();
		rightLayout.addComponent(textArea);

		updateInfoPanel.setSizeFull();
		updateInfoPanel.setSpacing(false);

		Panel bottom = new Panel("Updated by", updateInfoPanel);
		bottom.setSizeFull();
		rightLayout.addComponent(bottom);

		listSelect.addValueChangeListener(event1 ->
		{
			StorageFile file = myStorageService.findOne(AuthUtil.getUserId(), Integer.parseInt(event1.getValue().iterator().next()));

			String text = "not found";

			updateInfoPanel.removeAllComponents();

			if(file != null)
			{
				try
				{
					byte[] bytes = StreamUtil.loadFromStream(new UnsyncByteArrayInputStream(file.getFileData()));
					text = new String(bytes, StandardCharsets.UTF_8);
				}
				catch(Exception e)
				{
					text = ExceptionUtil.getThrowableText(e);
				}

				addFields(InformationBean.class, file.getUpdateBy(), updateInfoPanel);
				addFields(StorageFileUpdateBy.class, file.getUpdateBy(), updateInfoPanel);
			}

			textArea.setReadOnly(false);
			textArea.setValue(text);
			textArea.setReadOnly(true);
		});

		Map<String, String> captions = new HashMap<>();
		for(StorageFile file : files)
		{
			captions.put(String.valueOf(file.getId()), file.getFilePath());
		}
		listSelect.setDataProvider(new ListDataProvider<>(captions.keySet()));
		listSelect.setItemCaptionGenerator(captions::get);

		panel.setFirstComponent(listSelect);

		panel.setSecondComponent(rightLayout);
		panel.setSizeFull();

		addComponent(panel);
		setExpandRatio(panel, 1f);
	}

	private void addFields(Class clazz, Object value, VerticalLayout verticalLayout)
	{
		Field[] declaredFields = clazz.getDeclaredFields();
		for(Field declaredField : declaredFields)
		{
			declaredField.setAccessible(true);

			String name = declaredField.getName();

			HorizontalLayout layout = new HorizontalLayout();
			layout.setSpacing(false);
			layout.setMargin(false);
			layout.setWidth("100%");

			verticalLayout.addComponent(layout);
			layout.addComponent(TinyComponents.newLabel(name + ": "));
			try
			{
				Object fieldValue = declaredField.get(value);
				if(name.equalsIgnoreCase("time"))
				{
					fieldValue = new Date((Long) fieldValue);
				}

				TextField field = TinyComponents.newTextField(String.valueOf(fieldValue));
				field.setReadOnly(true);
				layout.addComponent(field);
			}
			catch(IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
