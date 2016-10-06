# hub.consulo.io [![Build Status](https://ci.consulo.io/view/consulo-webservices/job/hub.consulo.io/badge/icon)](https://ci.consulo.io/view/consulo-webservices/job/hub.consulo.io/)

Channels
 * release
 * beta
 * alpha
 * nightly
 * internal

Models
 * PluginNode
```json
  {
    "id": "",
    "name": "",
    "dependencies": ["id1", "id2"]
  }
```

Methods
 * **GET** /api/plugins/download?channel={channel}&platformVersion={platformVersion|SNAPSHOT}&pluginId={pluginId}
     * Return zip file with plugin, or 404
     * All parameters required
 * **GET** /api/plugins/list?channel={channel}&platformVersion={platformVersion|SNAPSHOT}
     * Return json with **PluginNode[]** (array of PluginNode)
 * **POST** /api/plugins/deploy?channel={channel}
     * Deploying plugin artifact(as zip file) to plugin manager.
     * Return json with **PluginNode** 
     * On error will send status code 403
