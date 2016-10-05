# Consulo webservice api [![Build Status](https://ci.consulo.io/buildStatus/icon?job=consulo-webservice-api)](https://ci.consulo.io/view/consulo-webservices/job/consulo-webservice-api/)(http://must-be.org/jenkins/view/consulo-webservices/job/consulo-webservice-api/)

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
 * **GET** /v2/consulo/plugins/download?channel={channel}&platformVersion={platformVersion|SNAPSHOT}&pluginId={pluginId}
     * Return zip file with plugin, or 404
     * All parameters required
 * **GET** /v2/consulo/plugins/list?channel={channel}&platformVersion={platformVersion|SNAPSHOT}&pretty={true|false}
     * Return json with **PluginNode[]** (array of PluginNode)
     * All parameters required, except **pretty**
 * **POST** /v2/consulo/plugins/deploy?channel={channel}
     * Deploying plugin artifact(as zip file) to plugin manager.
     * Return json with **PluginNode** 
     * On error will send status code 403
