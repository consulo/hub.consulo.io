package consulo.hub.frontend.base;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import consulo.hub.frontend.base.ui.util.VaadinUIUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.LocalDate;

/**
 * @author VISTALL
 * @since 18-Apr-17
 */
@Deprecated
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
		table.setSpacing(false);
		table.setMargin(false);
		table.setSizeFull();

		table.addComponent(myComponentContainer, 0, 0);

		HorizontalLayout bottomPanel = VaadinUIUtil.newHorizontalLayout();
		bottomPanel.setWidth(100, Unit.PERCENTAGE);
		bottomPanel.addStyleName("bottomPanel");
		bottomPanel.setSpacing(true);

		HorizontalLayout leftLayout = VaadinUIUtil.newHorizontalLayout();
		Label copyright = new Label(String.format("@ 2013 - %d consulo.io", LocalDate.now().getYear()));
		copyright.addStyleName(ValoTheme.LABEL_SMALL);
		copyright.addStyleName(ValoTheme.LABEL_LIGHT);
		leftLayout.addComponent(copyright);

		bottomPanel.addComponent(leftLayout);
		bottomPanel.setComponentAlignment(leftLayout, Alignment.MIDDLE_LEFT);

		HorizontalLayout rightLayout = VaadinUIUtil.newHorizontalLayout();
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

	@Nonnull
	public ComponentContainer getComponentContainer()
	{
		return myComponentContainer;
	}
}
