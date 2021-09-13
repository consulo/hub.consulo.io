package consulo.hub.backend.storage;

import consulo.hub.backend.storage.repository.StoragePluginRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.storage.domain.StoragePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 13/09/2021
 */
@RestController
@Transactional
public class StoragePluginsRestController
{
	public static class PluginInfo
	{
		public String id;
		public boolean enabled;

		public PluginInfo()
		{
		}

		public PluginInfo(String id, boolean enabled)
		{
			this.id = id;
			this.enabled = enabled;
		}
	}

	@Autowired
	private StoragePluginRepository myStoragePluginRepository;

	@RequestMapping(value = "/api/storage/plugins/list", method = RequestMethod.GET)
	public List<PluginInfo> pluginsList(@AuthenticationPrincipal UserAccount account) throws IOException
	{
		return listAll(account);
	}

	@RequestMapping(value = "/api/storage/plugins/merge", method = RequestMethod.POST)
	public List<PluginInfo> pluginsMerge(@AuthenticationPrincipal UserAccount account, @RequestBody PluginInfo[] pluginInfos)
	{
		if(pluginInfos.length == 0)
		{
			throw new IllegalArgumentException("empty");
		}

		for(PluginInfo pluginInfo : pluginInfos)
		{
			StoragePlugin storagePlugin = myStoragePluginRepository.findByUserAndPluginId(account, pluginInfo.id);
			if(storagePlugin == null)
			{
				storagePlugin = new StoragePlugin();
				storagePlugin.setUser(account);
				storagePlugin.setPluginId(pluginInfo.id);
			}

			storagePlugin.setEnabled(pluginInfo.enabled);

			myStoragePluginRepository.save(storagePlugin);
		}

		return listAll(account);
	}

	@RequestMapping(value = "/api/storage/plugins/delete", method = RequestMethod.POST)
	public List<PluginInfo> pluginsDelete(@AuthenticationPrincipal UserAccount account, @RequestBody PluginInfo[] pluginInfos)
	{
		if(pluginInfos.length == 0)
		{
			throw new IllegalArgumentException("empty");
		}

		List<StoragePlugin> forDelete = new ArrayList<>();
		for(PluginInfo pluginInfo : pluginInfos)
		{
			StoragePlugin storagePlugin = myStoragePluginRepository.findByUserAndPluginId(account, pluginInfo.id);
			if(storagePlugin != null)
			{
				forDelete.add(storagePlugin);
			}
		}

		myStoragePluginRepository.delete(forDelete);

		return listAll(account);
	}

	private List<PluginInfo> listAll(UserAccount account)
	{
		List<StoragePlugin> plugins = myStoragePluginRepository.findAllByUser(account);
		return plugins.stream().map(it -> new PluginInfo(it.getPluginId(), it.isEnabled())).collect(Collectors.toList());
	}
}
