package consulo.webService.ui;

import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.ValoTheme;

/**
 * @author VISTALL
 * @since 18-Apr-17
 */
public class RootContentView extends HorizontalLayout
{
	private final ComponentContainer myComponentContainer;

	public RootContentView(NavigationMenu menu)
	{
		setSizeFull();
		addStyleName("mainview");

		addComponent(menu);

		myComponentContainer = new CssLayout();
		myComponentContainer.addStyleName("view-content");
		myComponentContainer.setSizeFull();

		GridLayout table = new GridLayout(1, 2);
		table.setSizeFull();

		table.addComponent(myComponentContainer, 0, 0);

		HorizontalLayout bottomPanel = new HorizontalLayout();
		bottomPanel.setWidth(100, Unit.PERCENTAGE);
		bottomPanel.addStyleName("bottomPanel");
		bottomPanel.setSpacing(true);

		HorizontalLayout leftLayout = new HorizontalLayout();
		Label copyright = new Label(String.format("@ %d consulo.io", LocalDate.now().getYear()));
		copyright.addStyleName(ValoTheme.LABEL_SMALL);
		copyright.addStyleName(ValoTheme.LABEL_LIGHT);
		leftLayout.addComponent(copyright);

		bottomPanel.addComponent(leftLayout);
		bottomPanel.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

		HorizontalLayout rightLayout = new HorizontalLayout();
		Link link = new Link("hub.consulo.io", new ExternalResource("https://github.com/consulo/hub.consulo.io"));
		link.addStyleName(ValoTheme.LINK_SMALL);
		link.setIcon(FontAwesome.GITHUB);
		rightLayout.addComponent(link);

		bottomPanel.addComponent(rightLayout);
		bottomPanel.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);

		table.addComponent(bottomPanel, 0, 1);

		table.setRowExpandRatio(0, 1f);

		addComponent(table);
		setExpandRatio(table, 1.0f);
	}

	@NotNull
	public ComponentContainer getComponentContainer()
	{
		return myComponentContainer;
	}
}
