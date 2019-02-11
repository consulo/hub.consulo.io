package consulo.webService.storage.view;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.io.UnsyncByteArrayInputStream;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.storage.mongo.MongoStorageFile;
import consulo.webService.storage.mongo.MongoStorageFileRepository;
import consulo.webService.storage.mongo.MongoStorageFileUpdateBy;
import consulo.webService.ui.util.TinyComponents;
import consulo.webService.util.InformationBean;

/**
 * @author VISTALL
 * @since 19-Feb-17
 */
@SpringView(name = StorageView.ID)
public class StorageView extends VerticalLayout implements View
{
	public static final String ID = "storage";

	@Autowired
	private MongoStorageFileRepository myStorageFileRepository;

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

		List<MongoStorageFile> files = myStorageFileRepository.findByEmail(authentication.getName());

		Label label = new Label("Storage: ");
		label.addStyleName("headerMargin");
		addComponent(label);

		HorizontalSplitPanel panel = new HorizontalSplitPanel();

		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.setMargin(new MarginInfo(false, true, false, true));
		rightLayout.setSpacing(false);
		rightLayout.setSizeFull();

		ListSelect<String> listSelect = TinyComponents.newListSelect();
		listSelect.setSizeFull();

		TextArea textArea = TinyComponents.newTextArea();
		textArea.setCaption("Text: ");
		textArea.setSizeFull();
		rightLayout.addComponent(textArea);

		VerticalLayout updateInfoPanel = new VerticalLayout();
		updateInfoPanel.setSpacing(false);
		updateInfoPanel.setMargin(false);
		rightLayout.addComponent(updateInfoPanel);

		listSelect.addValueChangeListener(event1 ->
		{
			MongoStorageFile file = myStorageFileRepository.findOne(event1.getValue().iterator().next());

			String text = "not found";

			updateInfoPanel.removeAllComponents();

			if(file != null)
			{
				try
				{
					byte[] bytes = StreamUtil.loadFromStream(new UnsyncByteArrayInputStream(file.getData()));
					text = new String(bytes, StandardCharsets.UTF_8);
				}
				catch(Exception e)
				{
					text = ExceptionUtil.getThrowableText(e);
				}

				addFields(InformationBean.class, file.getUpdateBy(), updateInfoPanel);
				addFields(MongoStorageFileUpdateBy.class, file.getUpdateBy(), updateInfoPanel);
			}

			textArea.setReadOnly(false);
			textArea.setValue(text);
			textArea.setReadOnly(true);
		});

		Map<String, String> captions = new HashMap<>();
		for(MongoStorageFile file : files)
		{
			captions.put(file.getId(), file.getFilePath());
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
