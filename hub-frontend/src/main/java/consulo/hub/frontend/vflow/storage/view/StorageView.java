package consulo.hub.frontend.vflow.storage.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import consulo.hub.frontend.vflow.backend.service.BackendStorageService;
import consulo.hub.frontend.vflow.base.MainLayout;
import consulo.hub.shared.base.InformationBean;
import consulo.hub.shared.storage.domain.StorageFile;
import consulo.hub.shared.storage.domain.StorageFileUpdateBy;
import consulo.procoeton.core.util.AuthUtil;
import consulo.procoeton.core.vaadin.ui.LabeledLayout;
import consulo.procoeton.core.vaadin.ui.ServerOfflineVChildLayout;
import consulo.procoeton.core.vaadin.ui.util.TinyComponents;
import consulo.util.io.StreamUtil;
import consulo.util.io.UnsyncByteArrayInputStream;
import consulo.util.lang.ExceptionUtil;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 19-Feb-17
 */
@PageTitle("Storage")
@Route(value = "user/storage", layout = MainLayout.class)
@PermitAll
public class StorageView extends ServerOfflineVChildLayout
{
	public static final String ID = "storage";

	private final BackendStorageService myBackendStorageService;

	private final Button myWipeDataButton;

	private final ListBox<Map.Entry<String, Long>> myItems;

	private final VerticalLayout myUpdateInfoPanel;

	@Autowired
	public StorageView(BackendStorageService backendStorageService)
	{
		super(true);

		myBackendStorageService = backendStorageService;
		myItems = new ListBox<>();
		myUpdateInfoPanel = new VerticalLayout();
		myWipeDataButton = new Button("Wipe All");
		
		myWipeDataButton.addClickListener(event -> {
			myBackendStorageService.deleteAll(AuthUtil.getUserId());

			myItems.setItems(new ArrayList<>());
			myItems.setItemLabelGenerator(s -> "");
			myUpdateInfoPanel.removeAll();
		});
	}

	@Override
	public Component getHeaderRightComponent()
	{
		return myWipeDataButton;
	}

	@Override
	protected void buildLayout(Consumer<Component> uiBuilder)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null)
		{
			return;
		}

		myUpdateInfoPanel.removeAll();

		List<StorageFile> files = myBackendStorageService.listAll(AuthUtil.getUserId());

		HorizontalLayout panel = new HorizontalLayout();

		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.addClassName(LumoUtility.Overflow.AUTO);
		rightLayout.setSpacing(false);
		rightLayout.setSizeFull();

		myItems.setSizeFull();

		TextArea textArea = new TextArea();
		textArea.setLabel("Text: ");
		textArea.setSizeFull();
		rightLayout.add(textArea);

		myUpdateInfoPanel.setSizeFull();
		myUpdateInfoPanel.setSpacing(false);

		LabeledLayout bottom = new LabeledLayout("Updated by", myUpdateInfoPanel);
		bottom.setSizeFull();
		rightLayout.add(bottom);

		myItems.addValueChangeListener(event1 ->
		{
			StorageFile file = myBackendStorageService.find(AuthUtil.getUserId(), event1.getValue().getValue());

			String text = "not found";

			myUpdateInfoPanel.removeAll();

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

				addFields(InformationBean.class, file.getUpdateBy(), myUpdateInfoPanel);
				addFields(StorageFileUpdateBy.class, file.getUpdateBy(), myUpdateInfoPanel);
			}

			textArea.setReadOnly(false);
			textArea.setValue(text);
			textArea.setReadOnly(true);
		});

		Map<String, Long> captions = new TreeMap<>();
		for(StorageFile file : files)
		{
			captions.put(file.getFilePath(), file.getId());
		}
		myItems.setItems(captions.entrySet());
		myItems.setItemLabelGenerator(Map.Entry::getKey);

		panel.add(myItems);
		panel.add(rightLayout);
		panel.setSizeFull();

		uiBuilder.accept(panel);
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
			layout.setWidthFull();

			verticalLayout.add(layout);
			layout.add(new Span(name + ": "));
			try
			{
				Object fieldValue = declaredField.get(value);
				if(name.equalsIgnoreCase("time"))
				{
					fieldValue = new Date((Long) fieldValue);
				}

				TextField field = TinyComponents.newTextField(String.valueOf(fieldValue));
				field.setWidthFull();
				field.setReadOnly(true);
				layout.add(field);
			}
			catch(IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
