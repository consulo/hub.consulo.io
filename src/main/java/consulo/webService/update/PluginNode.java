package consulo.webService.update;

import java.io.File;

/**
 * @author VISTALL
 * @since 30-Aug-16
 */
public class PluginNode
{
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
}
