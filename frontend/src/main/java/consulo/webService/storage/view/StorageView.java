package consulo.webService.storage.view;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.iq80.snappy.SnappyInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.io.UnsyncByteArrayInputStream;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import consulo.webService.storage.mongo.MongoStorageFile;
import consulo.webService.storage.mongo.MongoStorageFileRepository;
import consulo.webService.ui.util.TidyComponents;

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

		setSizeFull();

		Label label = new Label("Storage: ");
		label.addStyleName("headerMargin");
		addComponent(label);

		HorizontalSplitPanel panel = new HorizontalSplitPanel();

		ListSelect listSelect = TidyComponents.newListSelect();
		TextArea textArea = new TextArea("Text: ");
		listSelect.addValueChangeListener(event1 ->
		{
			MongoStorageFile file = myStorageFileRepository.findOne((String) event1.getProperty().getValue());

			String text = "not found";

			if(file != null)
			{
				try (SnappyInputStream inputStream = new SnappyInputStream(new UnsyncByteArrayInputStream(file.getData())))
				{
					byte[] bytes = StreamUtil.loadFromStream(inputStream);
					text = new String(bytes, StandardCharsets.UTF_8);
				}
				catch(Exception e)
				{
					text = ExceptionUtil.getThrowableText(e);
				}
			}

			textArea.setReadOnly(false);
			textArea.setValue(text);
			textArea.setReadOnly(true);
		});

		textArea.setSizeFull();
		listSelect.setSizeFull();
		for(MongoStorageFile file : files)
		{
			listSelect.addItem(file.getId());
			listSelect.setItemCaption(file.getId(), file.getFilePath());
		}

		panel.setFirstComponent(listSelect);

		panel.setSecondComponent(textArea);
		panel.setSizeFull();

		addComponent(panel);
		setExpandRatio(panel, 1f);
	}
}
