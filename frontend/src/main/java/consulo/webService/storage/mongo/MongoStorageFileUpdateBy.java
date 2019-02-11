package consulo.webService.storage.mongo;

import consulo.webService.util.InformationBean;

/**
 * @author VISTALL
 * @since 2019-02-11
 */
public class MongoStorageFileUpdateBy extends InformationBean
{
	private long time;

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}
}
