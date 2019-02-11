package consulo.webService.storage.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import consulo.webService.util.InformationBean;

/**
 * @author VISTALL
 * @since 2019-02-11
 */
@JsonIgnoreProperties
public class PushFileBeanRequest extends InformationBean
{
	private String bytes;
	private String filePath;

	public String getBytes()
	{
		return bytes;
	}

	public void setBytes(String bytes)
	{
		this.bytes = bytes;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}
}
