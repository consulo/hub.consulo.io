package consulo.hub.backend.storage;

import consulo.hub.backend.storage.repository.StoragePluginRepository;
import consulo.hub.shared.auth.domain.UserAccount;
import consulo.hub.shared.storage.domain.StoragePlugin;
import consulo.hub.shared.storage.domain.StoragePluginState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.IOException;
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

		public StoragePluginState state;

		public PluginInfo()
		{
		}

		public PluginInfo(String id, StoragePluginState state)
		{
			this.id = id;
			this.state = state;
		}

		@Override
		public String toString()
		{
			return "PluginInfo{" +
					"id='" + id + '\'' +
					", state=" + state +
					'}';
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
		for(PluginInfo pluginInfo : pluginInfos)
		{
			if(pluginInfo.id == null || pluginInfo.state == null || pluginInfo.state == StoragePluginState.UNINSTALLED)
			{
				// we not allow set UNINSTALLED in this call
				throw new IllegalArgumentException(pluginInfo.toString());
			}

			StoragePlugin storagePlugin = myStoragePluginRepository.findByUserAndPluginId(account, pluginInfo.id);
			if(storagePlugin == null)
			{
				storagePlugin = new StoragePlugin();
				storagePlugin.setUser(account);
				storagePlugin.setPluginId(pluginInfo.id);
			}
			else if(storagePlugin.getPluginState() == StoragePluginState.UNINSTALLED)
			{
				// we not allow override UNINSTALLED plugin state - use add for it
				continue;
			}

			storagePlugin.setPluginState(pluginInfo.state);

			myStoragePluginRepository.save(storagePlugin);
		}

		return listAll(account);
	}

	@RequestMapping(value = "/api/storage/plugins/add", method = RequestMethod.POST)
	public List<PluginInfo> pluginsAdd(@AuthenticationPrincipal UserAccount account, @RequestBody PluginInfo[] pluginInfos)
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

			storagePlugin.setPluginState(StoragePluginState.ENABLED);

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

		for(PluginInfo pluginInfo : pluginInfos)
		{
			StoragePlugin storagePlugin = myStoragePluginRepository.findByUserAndPluginId(account, pluginInfo.id);
			if(storagePlugin == null)
			{
				storagePlugin = new StoragePlugin();
				storagePlugin.setUser(account);
				storagePlugin.setPluginId(pluginInfo.id);
			}

			storagePlugin.setPluginState(StoragePluginState.UNINSTALLED);

			myStoragePluginRepository.save(storagePlugin);
		}

		return listAll(account);
	}

	private List<PluginInfo> listAll(UserAccount account)
	{
		List<StoragePlugin> plugins = myStoragePluginRepository.findAllByUser(account);
		return plugins.stream().map(it -> new PluginInfo(it.getPluginId(), it.getPluginState())).collect(Collectors.toList());
	}
}
