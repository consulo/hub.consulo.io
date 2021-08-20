package consulo.hub.frontend.base.ui.event;

import com.vaadin.navigator.View;

/**
 * @author VISTALL
 * @since 18-Apr-17
 */
public class AfterViewChangeEvent
{
	private View myView;

	public AfterViewChangeEvent(View view)
	{
		myView = view;
	}

	public View getView()
	{
		return myView;
	}
}
