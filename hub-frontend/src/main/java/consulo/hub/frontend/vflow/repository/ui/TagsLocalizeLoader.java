package consulo.hub.frontend.vflow.repository.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 03/11/2021
 */
@Service
public class TagsLocalizeLoader
{
	private static final Logger LOG = LoggerFactory.getLogger(TagsLocalizeLoader.class);

	private Map<String, String> myTagsText = new HashMap<>();

	//@PostConstruct  TODO
	public void init()
	{
		try (InputStream resourceAsStream = TagsLocalizeLoader.class.getResourceAsStream("/localize/consulo/platform/base/RepositoryTagLocalize.yaml"))
		{
			Yaml yaml = new Yaml();

			Map<String, Map<String, String>> data = yaml.load(resourceAsStream);

			for(Map.Entry<String, Map<String, String>> entry : data.entrySet())
			{
				String key = entry.getKey();

				Map<String, String> value = entry.getValue();

				String text = value.get("text");

				myTagsText.put(key, text);
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	public String getTagLocalize(String tagId)
	{
		return myTagsText.getOrDefault(tagId, tagId);
	}
}
