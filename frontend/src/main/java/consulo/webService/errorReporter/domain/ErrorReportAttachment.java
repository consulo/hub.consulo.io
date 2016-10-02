package consulo.webService.errorReporter.domain;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;

/**
* @author VISTALL
* @since 02-Oct-16
*/
@Document(collection = "errorReportAttachment")
public class ErrorReportAttachment implements Serializable
{
	private final String id = UUID.randomUUID().toString();

	private String name;
	private String path;
	private String encodedText;

	ErrorReportAttachment()
	{
	}

	ErrorReportAttachment(String name, String path, String encodedText)
	{
		this.name = name;
		this.path = path;
		this.encodedText = encodedText;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getEncodedText()
	{
		return encodedText;
	}

	public void setEncodedText(String encodedText)
	{
		this.encodedText = encodedText;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(!(o instanceof ErrorReportAttachment))
		{
			return false;
		}

		ErrorReportAttachment that = (ErrorReportAttachment) o;

		if(!id.equals(that.id))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}
