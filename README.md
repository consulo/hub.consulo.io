# hub.consulo.io [![Build Status](https://ci.consulo.io/job/hub.consulo.io/badge/icon)](https://ci.consulo.io/job/hub.consulo.io/)

Channels
 * release
 * beta
 * alpha
 * nightly

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
 * **GET** /api/repository/download?channel={channel}&platformVersion={platformVersion|SNAPSHOT}&pluginId={pluginId}
     * Return zip file with plugin, or 404
     * All parameters required

 * **GET** /api/repository/list?channel={channel}&platformVersion={platformVersion|SNAPSHOT}
     * Return json with **PluginNode[]** (array of PluginNode)

 * **POST** /api/repository/pluginDeploy?channel={channel}
     * Deploying plugin artifact(as zip file) to repository.
     * Return json with **PluginNode** 
     * On error will send status code 403

 * **POST** /api/repository/platformDeploy?channel={channel}
    * Deploying platform artifact(as tar.gz file) to repository.
    * Return json with **PluginNode**
    * On error will send status code 403
