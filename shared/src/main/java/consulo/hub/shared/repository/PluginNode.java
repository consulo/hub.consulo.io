package consulo.hub.shared.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginNode implements Cloneable
{
	public static class Extension
	{
		public String key;

		public String[] values = ArrayUtils.EMPTY_STRING_ARRAY;
	}

	public static class Permission
	{
		public String type;

		public String[] options;
	}

	public static class Checksum
	{
		public String md5;
		public String sha_256;
		public String sha3_256;
	}

	private static final String CORE_PLUGIN = "com.intellij";

	public static final PluginNode[] EMPTY_ARRAY = new PluginNode[0];

	public String id;
	public String name;
	public String description;
	public String category;
	public String vendor;
	public Checksum checksum = new Checksum();
	public Integer downloads;
	public Long length;
	public Long date;
	public String version;
	public String platformVersion;
	public String[] dependencies;
	public String[] optionalDependencies;
	public String[] incompatibleWiths;
	public String iconBytes;

	public Extension[] extensions;
	public Extension[] extensionsV2;

	public Permission[] permissions;

	public String[] tags;

	public boolean experimental;

	public transient File targetFile;

	public void clean()
	{
		dependencies = dependencies == null ? ArrayUtils.EMPTY_STRING_ARRAY : ArrayUtils.removeElement(dependencies, CORE_PLUGIN);
		optionalDependencies = optionalDependencies == null ? ArrayUtils.EMPTY_STRING_ARRAY : ArrayUtils.removeElement(optionalDependencies, CORE_PLUGIN);

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

	@Override
	public PluginNode clone()
	{
		try
		{
			PluginNode clone = (PluginNode) super.clone();
			clone.targetFile = null;
			return clone;
		}
		catch(CloneNotSupportedException e)
		{
			throw new Error(e);
		}
	}
}
