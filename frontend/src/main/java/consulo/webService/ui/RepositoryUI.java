package consulo.webService.ui;

import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import consulo.webService.UserConfigurationService;
import consulo.webService.plugins.PluginChannel;
import consulo.webService.plugins.PluginChannelService;
import consulo.webService.plugins.PluginNode;

/**
 * @author VISTALL
 * @since 09-Nov-16
 */
@SpringUI(path = "repo")
// No @Push annotation, we are going to enable it programmatically when the user logs on
@Theme("tests-valo-metro")
@StyleSheet("https://fonts.googleapis.com/css?family=Roboto")
public class RepositoryUI extends BaseUI
{
	@Autowired
	private UserConfigurationService myUserConfigurationService;

	@Override
	protected void initImpl(VaadinRequest request, Page page)
	{
		getPage().setTitle("Platform & Plugin Repository");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();

		ListSelect listSelect = new ListSelect();
		listSelect.setNullSelectionAllowed(false);
		listSelect.setHeight(100, Unit.PERCENTAGE);
		listSelect.setWidth(40, Unit.PERCENTAGE);
		layout.addComponent(listSelect);

		PluginChannelService repositoryByChannel = myUserConfigurationService.getRepositoryByChannel(PluginChannel.nightly);

		PluginNode[] select = repositoryByChannel.select(PluginChannelService.SNAPSHOT, false);

		for(PluginNode pluginNode : select)
		{
			listSelect.addItem(pluginNode.id);
			listSelect.setItemCaption(pluginNode.id, pluginNode.name);
		}
		setContent(layout);
	}
}
