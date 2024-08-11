package consulo.hub.shared.errorReporter.domain;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Entity
public class ErrorReportAttachment
{
	@Id
	@GeneratedValue
	private Long id;

	private String name;
	@Column(length = 1024)
	private String path;
	@Column(length = 10485760)
	private String encodedText;

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

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		ErrorReportAttachment that = (ErrorReportAttachment) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id);
	}
}
