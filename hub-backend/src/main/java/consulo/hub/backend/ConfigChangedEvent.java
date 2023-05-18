package consulo.hub.backend;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
public class ConfigChangedEvent extends ApplicationEvent
{
	public ConfigChangedEvent(Object source)
	{
		super(source);
	}

	public ConfigChangedEvent(Object source, Clock clock)
	{
		super(source, clock);
	}
}
