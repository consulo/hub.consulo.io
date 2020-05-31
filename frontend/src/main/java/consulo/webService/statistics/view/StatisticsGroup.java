package consulo.webService.statistics.view;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author VISTALL
 * @since 2020-05-31
 */
public class StatisticsGroup
{
	private final String myId;

	private Map<String, AtomicInteger> counts = new HashMap<>();

	public StatisticsGroup(String id)
	{
		myId = id;
	}

	public String getId()
	{
		return myId;
	}

	public void incData(String key, int count)
	{
		AtomicInteger value = counts.computeIfAbsent(key, s -> new AtomicInteger());

		value.addAndGet(count);
	}

	public Map<String, AtomicInteger> getCounts()
	{
		return counts;
	}
}
