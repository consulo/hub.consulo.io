# Consulo webservice

Channels
 * release
 * beta
 * alpha
 * nightly
 * internal

API
 * **GET** /v2/consulo/plugins/download?channel={channel}&platformVersion={platformVersion|SNAPSHOT}&pluginId={pluginId}
     * Return zip file with plugin, or 404
     * All parameters required
 * **GET** /v2/consulo/plugins/list?channel={channel}&platformVersion={platformVersion|SNAPSHOT}&pretty={true|false}
     * Return json file with plugin list
     * All parameters required, except **pretty**
 * **POST** /v2/consulo/plugins/deploy?channel={channel}
     * Deploying plugin artifact(as zip file) to plugin manager.
     * On error will send status code 403
 * **GET** /v2/status
    * Return json file contains status of api
