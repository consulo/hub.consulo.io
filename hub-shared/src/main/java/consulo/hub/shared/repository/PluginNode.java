package consulo.hub.shared.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginNode implements Cloneable
{
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ExtensionPreview
	{
		public String apiPluginId;
		public String apiClassName;

		public String implId;
		//public String implPluginId;

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder("ExtensionPreview{");
			sb.append("apiPluginId='").append(apiPluginId).append('\'');
			sb.append(", apiClassName='").append(apiClassName).append('\'');
			sb.append(", implId='").append(implId).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Permission
	{
		public String type;

		public String[] options;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Checksum
	{
		public String md5;
		public String sha_256;
		public String sha3_256;
	}

	private static final String CONSULO_PLUGIN = "consulo";

	public static final PluginNode[] EMPTY_ARRAY = new PluginNode[0];

	public String id;
	public String name;
	public String description;
	public String category;
	public String vendor;
	public String vendorUrl;
	public String vendorEmail;
	public String url;
	public Checksum checksum = new Checksum();
	public Integer downloads;
	public Integer downloadsAll;
	public Long length;
	public Long date;
	public String version;
	public String platformVersion;
	public String[] dependencies;
	public String[] optionalDependencies;
	public String[] incompatibleWiths;
	public String iconBytes;
	public String iconDarkBytes;

	public String[] downloadUrls;

	public ExtensionPreview[] extensionPreviews;

	public Permission[] permissions;

	public String[] tags;

	public boolean experimental;

	// old store ref
	@Deprecated
	public transient File targetFile;
	// new store ref
	public transient Path targetPath;

	public void cleanUp()
	{
		dependencies = dependencies == null ? ArrayUtils.EMPTY_STRING_ARRAY : ArrayUtils.removeElement(dependencies, CONSULO_PLUGIN);
		optionalDependencies = optionalDependencies == null ? ArrayUtils.EMPTY_STRING_ARRAY : ArrayUtils.removeElement(optionalDependencies, CONSULO_PLUGIN);

		if(dependencies.length == 0)
		{
			dependencies = null;
		}

		if(optionalDependencies.length == 0)
		{
			optionalDependencies = null;
		}

		if(incompatibleWiths != null && incompatibleWiths.length == 0)
		{
			incompatibleWiths = null;
		}

		if(permissions != null && permissions.length == 0)
		{
			permissions = null;
		}

		if(tags != null && tags.length == 0)
		{
			tags = null;
		}
	}

	public ZipFile openZip() throws IOException
	{
		if(targetPath != null)
		{
			return new ZipFile(targetPath.toFile());
		}

		if(targetFile != null)
		{
			return new ZipFile(targetFile);
		}

		throw new IllegalArgumentException(id);
	}

	@Override
	public PluginNode clone()
	{
		try
		{
			PluginNode clone = (PluginNode) super.clone();
			clone.targetFile = null;
			clone.targetPath = null;
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			throw new Error(e);
		}
	}
}
