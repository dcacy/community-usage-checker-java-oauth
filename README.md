# community-usage-checker-java-oauth

Instructions:

This sample application lists all the Connections Cloud Communities which can be viewed by the ID you provide, and also shows various metrics for each Community.

For a node.js version of the same application, see [here](https://github.com/dcacy/community-usage-checker-oauth).


## Getting started

1. Create an **Internal Application** entry in your Connections Cloud environment. See [here](https://www-10.lotus.com/ldd/appdevwiki.nsf/xpAPIViewer.xsp?lookupName=API+Reference#action=openDocument&res_title=Step_1_Register_the_application_sbt&content=apicontent&sa=true) for info.

  - The Auth Type should be `OAuth 2.0`.

  - The callback URL should be your uri plus `/api?action=oauthback`, ex. `https://server.com/community-usage-checker-java-oauth/api?action=oauthback`.

2. Provide the following environment variables to your application:

  ```none
	COMMUNITY_USAGE_CHECKER_DEBUG=true (optional)
	COMMUNITY_USAGE_CHECKER_HOSTNAME=<host name of Connections Cloud, ex. apps.na.collabserv.com>
	COMMUNITY_USAGE_CHECKER_CLIENT_ID=<client id from the above step>
	COMMUNITY_USAGE_CHECKER_CLIENT_SECRET=<client secret from the above step>
	VCAP_APPLICATION={"application_uris":["<your uri, ex. 69984aa2.ngrok.io/community-usage-checker-java-oauth>"]}
  ```

3. Download `date.format.js` from [https://gist.github.com/eralston/968809](https://gist.github.com/eralston/968809) and copy it to the `src/main/webapp/js` directory.


## To run the application locally using WebSphere Liberty

1. Install the maven dependencies identified in the `pom.xml` file.

1. Add the above environment variables to `server.env`.

1. Use a tool like `ngrok` to provide a URL to your application. Connections Cloud requires an `https` URI to point to.

1. Load the application in your browser. When you click `Log In`, you'll be taken to Connections Cloud. Once you authenticate, you'll be prompted to allow this application to access your Connections Cloud data. If you approve, you'll be directed back to the app, where it will attempt to retrieve the Communities to which you have access.

1. Click on a Community row to see its details!

Please read the LICENSE file for copyright and license information.
