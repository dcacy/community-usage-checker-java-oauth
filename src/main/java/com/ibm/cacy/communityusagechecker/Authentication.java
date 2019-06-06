package com.ibm.cacy.communityusagechecker;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Authentication {

	private boolean DEBUG = Boolean.parseBoolean(System.getenv("COMMUNITY_USAGE_CHECKER_DEBUG"));
	private Logger logger = Logger.getLogger(Authentication.class.getName());

	private final String CONNECTIONS_HOST = System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME");
	private final String CC_OAUTH_URL = "/manage/oauth2/authorize";
	private final String AUTHORIZATION_API = "/manage/oauth2/token";
	private final String CLIENT_ID = System.getenv("COMMUNITY_USAGE_CHECKER_CLIENT_ID");
	private final String CLIENT_SECRET = System.getenv("COMMUNITY_USAGE_CHECKER_CLIENT_SECRET");
	private final JsonObject vcap_application = new JsonParser().parse(System.getenv("VCAP_APPLICATION")).getAsJsonObject();
	private final String APP_HOSTNAME = vcap_application.get("application_uris").getAsJsonArray().get(0).getAsString();
	


	/**
	 * get the oauth token
	 * @param code the string we received from Connections (the service provider)
	 * @return {Object} a map of the results of the call, including the access token
	 * @throws Exception
	 */
	public Map<String, String> getAccessToken(String code) throws Exception
	{
		// set up the call
		Executor executor = Executor.newInstance();
		URI serviceURI = new URI("https://" + CONNECTIONS_HOST + AUTHORIZATION_API).normalize();
		List<BasicNameValuePair> parametersBody = new ArrayList<BasicNameValuePair>();
		parametersBody.add(new BasicNameValuePair("grant_type", "authorization_code"));
		parametersBody.add(new BasicNameValuePair("code", code));
		parametersBody.add(new BasicNameValuePair("client_id", CLIENT_ID));
		parametersBody.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
		parametersBody.add(new BasicNameValuePair("redirect_uri", "https://" + APP_HOSTNAME + "/api?action=oauthback"));

		String authResult = 
			executor.execute(Request.Post(serviceURI)
				.addHeader("content-type", "application/x-www-form-urlencoded")
				.bodyForm(parametersBody)
			).returnContent().asString();
	
		// if authentication/authorization succeeded, then we should get a query string
		// if it failed, we should get an error message
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    if (authResult.indexOf("&") > -1)
	    {
		    String[] pairs = authResult.split("&");
		    for (String pair : pairs) 
		    {
		        int idx = pair.indexOf("=");
		        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		    }
	    } else {
	   	 query_pairs.put("error", authResult);
	    }
	    return query_pairs;
	}

	private void log(Object o) 
	{
		if (DEBUG)
			logger.info(o.toString());
	}
}
