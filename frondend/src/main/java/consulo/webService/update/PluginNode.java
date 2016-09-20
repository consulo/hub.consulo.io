package consulo.webService.update;

import java.io.File;

import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
public class PluginNode implements Cloneable
{
	private static final String CORE_PLUGIN = "com.intellij";

	public static final PluginNode[] EMPTY_ARRAY = new PluginNode[0];

	public String id;
	public String name;
	public String description;
	public String category;
	public String vendor;
	public Integer downloads;
	public Long length;
	public Long date;
	public Integer rating;
	public String version;
	public String platformVersion;
	public String[] dependencies;
	public String[] optionalDependencies;

	public transient File targetFile;

	public void clean()
	{
		dependencies = dependencies == null ? ArrayUtil.EMPTY_STRING_ARRAY : ArrayUtil.remove(dependencies, CORE_PLUGIN);
		optionalDependencies = optionalDependencies == null ? ArrayUtil.EMPTY_STRING_ARRAY : ArrayUtil.remove(optionalDependencies, CORE_PLUGIN);

		if(dependencies.length == 0)
		{
			dependencies = null;
		}

		if(optionalDependencies.length == 0)
		{
			optionalDependencies = null;
		}
	}

	@Override
	protected PluginNode clone()
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
