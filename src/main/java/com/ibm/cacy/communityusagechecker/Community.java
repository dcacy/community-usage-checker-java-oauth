package com.ibm.cacy.communityusagechecker;

import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import com.ibm.json.java.JsonArray;
//import com.ibm.json.java.JsonObject;
import com.ibm.json.xml.XMLToJSONTransformer;

public class Community {

	private static boolean DEBUG = Boolean.parseBoolean(System.getenv("COMMUNITY_USAGE_CHECKER_DEBUG"));
	private static Logger logger = Logger.getLogger(Community.class.getName());

	/**
	 * Get all the communities in Connections
	 * @param {String} token is the oauth access token
	 * @param {boolean} true = show 500 communities, false = 50
	 * @return {JsonObject} containing community info
	 */
	public JsonObject getAllCommunities(String token, boolean showAll) 
	{
		debug("-------> entering getAllCommunities");
		String howMany = showAll ? "500" : "50";
		final String ALL_COMMUNITIES_URI = "/communities/service/atom/communities/all?ps=" + howMany;
		JsonObject result = new JsonObject();
		try 
		{
			// call the REST API to get communities
			String url = "https://" + System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME") + ALL_COMMUNITIES_URI;
			Executor executor = Executor.newInstance();
	      InputStream communityXmlStream = executor.execute(Request.Get(url)
	      	.addHeader("Authorization", String.format("Bearer %s", token))
	      	).returnContent().asStream();

	      // transform the XML to JSON
    		String jsonString = XMLToJSONTransformer.transform(communityXmlStream);
    		JsonObject communityJson = new JsonParser().parse(jsonString).getAsJsonObject();
    		
    		// iterate through the results to get the data we want
    		JsonArray entries = communityJson.get("feed").getAsJsonObject().get("entry").getAsJsonArray();
			Iterator<JsonElement> it = entries.iterator();
    		JsonArray communityInfo = new JsonArray();
    		while ( it.hasNext() ) {
    			JsonObject entry = it.next().getAsJsonObject();
    			JsonObject info = new JsonObject();
    			info.add("title", entry.get("title").getAsJsonObject().get("content"));
    			info.add("id",  entry.get("communityUuid"));
    			info.add("updated", ((entry.get("updated"))));
    			info.add("owner", entry.get("author").getAsJsonObject().get("name"));
    			info.add("created", entry.get("published"));
    			info.add("membercount", entry.get("membercount"));
				info.add("type", entry.get("communityType"));

    			communityInfo.add(info);
    		}
    		result.add("communityInfo", communityInfo);
    		JsonObject userIdentity = getUserIdentity(token);
    		result.add("name", userIdentity.get("name"));
    		result.add("email", userIdentity.get("email"));
    		
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			result.addProperty("error", e.getMessage());
		}
		
		debug("<------- exiting getAllCommunities");
		return result;
	}
	
	/**
	 * Get the members of a Community
	 * @param {String} the oauth access token
	 * @param id of the Community
	 * @return {JsonObject} containing the members of a community
	 */
	public JsonObject getCommunityMembers(String token, String id) {
		debug("-------> entering getCommunityMembers with id " + id);
		JsonObject result = new JsonObject();
		final String COMM_MEMBERS_URI = "/communities/service/atom/community/members?ps=1000&communityUuid=";
		
		try 
		{
			Executor executor = Executor.newInstance();
			String url = "https://" + System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME") + COMM_MEMBERS_URI;
			URI serviceURI = new URI(url + id).normalize();
			InputStream membersXmlStream = executor.execute(Request.Get(serviceURI)
		      .addHeader("Authorization", String.format("Bearer %s", token))
			   ).returnContent().asStream();
			
			// use XML tools to get the data we want
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDoc = builder.parse(membersXmlStream);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList entries = (NodeList) xPath.compile("/feed/entry").evaluate(xmlDoc, XPathConstants.NODESET);
			JsonArray members = new JsonArray();
			for ( int i = 0; i < entries.getLength(); i++ ) {
				Node entry = entries.item(i);
				Node contributor = (Node) xPath.compile("contributor").evaluate(entry, XPathConstants.NODE);
				NodeList children = contributor.getChildNodes();
				JsonObject member = new JsonObject();
				for (int j = 0; j < children.getLength(); j++ ){
					if ( children.item(j).getNodeName().equals("email")) {
						member.addProperty("email", children.item(j).getTextContent());
					} else if ( children.item(j).getNodeName().equals("name")) {
						member.addProperty("name", children.item(j).getTextContent());
					} else if ( children.item(j).getNodeName().equals("snx:userState")) {
						member.addProperty("state", children.item(j).getTextContent());
					}
				}
				// if member is inactive, there is no email, so put in empty string
				if (member.get("email") == null) 
				{
					member.addProperty("email", "");
				}
				members.add(member);
				result.addProperty("type",  "members");
				result.add("data",  members);
			}
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		debug("-------> exiting getCommunityMembers with id " + id);
		return result;
	}

	/**
	 * get the files in a Community
	 * @param {String} the oauth access token
	 * @param {String} id of the Community
	 * @return {JsonObject) containing the files of a Community
	 */
	public JsonObject getCommunityFiles(String token, String id) {
		debug("-------> entering getCommunityFiles with id " + id);
		JsonObject result = new JsonObject();
		final String COMM_FILES_URI = "/files/basic/api/communitycollection/"
				+ id
				+ "/feed?sC=document&pageSize=500&sortBy=title&type=communityFiles";
		
		try {
			Executor executor = Executor.newInstance();
			String url = "https://" + System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME") + COMM_FILES_URI;
			URI serviceURI = new URI(url).normalize();

			InputStream filesXmlStream = executor.execute(Request.Get(serviceURI)
			    .addHeader("Authorization", String.format("Bearer %s", token))
			    ).returnContent().asStream();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDoc = builder.parse(filesXmlStream);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList entries = (NodeList) xPath.compile("/feed/entry").evaluate(xmlDoc, XPathConstants.NODESET);
			JsonArray files = new JsonArray();
			for ( int i = 0; i < entries.getLength(); i++ ) {
				Node entry = entries.item(i);
				String fileLength = (String) xPath.compile("link/@length").evaluate(entry, XPathConstants.STRING);
				Node title = (Node)xPath.compile("title").evaluate(entry, XPathConstants.NODE);
				JsonObject file = new JsonObject();
				file.addProperty("title", title.getTextContent());
				file.addProperty("size",  fileLength);
				files.add(file);
			}
			result.addProperty("type", "files");
			result.add("data",  files);

		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		debug("-------> exiting getCommunityFiles with id " + id);
		return result;
	}

	/**
	 * get the Subcommunities of a Community
	 * @param {String} the oauth access token
	 * @param {String} id of the Community
	 * @return {JsonObject} containing the subcommunities
	 */
	public JsonObject getSubcommunities(String token, String id) {
		debug("------> entering getSubcommunities with id " + id);
		JsonObject result = new JsonObject();
		final String SUBCOMMUNITIES_URI = "/communities/service/atom/community/subcommunities?communityUuid="
				+ id;
		
		try 
		{
			Executor executor = Executor.newInstance();//.auth(properties.getProperty("CONNECTIONS_USERID"), properties.getProperty("CONNECTIONS_PASSWORD"));
			URI serviceURI = new URI("https://" + System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME") + SUBCOMMUNITIES_URI).normalize();
			
			InputStream subcommunitiesXmlStream = 
				executor.execute(Request.Get(serviceURI)
			   .addHeader("Authorization", String.format("Bearer %s", token)))
				.returnContent().asStream();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDoc = builder.parse(subcommunitiesXmlStream);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			NodeList entries = (NodeList) xPath.compile("/feed/entry").evaluate(xmlDoc, XPathConstants.NODESET);
			JsonArray subcommunities = new JsonArray();
			for ( int i = 0; i < entries.getLength(); i++ ) {
				Node entry = entries.item(i);
				Node title = (Node)xPath.compile("title").evaluate(entry, XPathConstants.NODE);
				JsonObject subcommunity = new JsonObject();
				subcommunity.addProperty("title", title.getTextContent());
				Node communityType = (Node)xPath.compile("//*[local-name()='communityType']/text()").evaluate(entry, XPathConstants.NODE);
				subcommunity.addProperty("type", communityType.getTextContent());
				subcommunities.add(subcommunity);
			}
			result.addProperty("type", "subcommunities");
			result.add("data",  subcommunities);

		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		debug("------> exiting getSubcommunities with id " + id);
		return result;
	}

	/**
	 * get the actitivies of a Community
	 * @param {Properties} properties containing the username, password, and host name
	 * @param id of the Community
	 * @return {JsonObject} containing the activities
	 */
	public JsonObject getCommunityActivity(String token, String id) {
		debug("------> entering getCommunityActivity with id " + id);
		JsonObject result = new JsonObject();
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -30); // one month ago
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String oneMonthAgo = df.format(cal.getTime());
		final String COMM_ACTIVITY_URI = "/connections/opensocial/basic/rest/activitystreams/urn:lsid:lconn.ibm.com:communities.community:"
			+ id 
			+ "/@all/@all?rollup=true&shortStrings=true&format=json&updatedSince" + oneMonthAgo;

		try {
			Executor executor = Executor.newInstance();//.auth(properties.getProperty("CONNECTIONS_USERID"), properties.getProperty("CONNECTIONS_PASSWORD"));
			URI serviceURI = new URI("https://" + System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME") + COMM_ACTIVITY_URI).normalize();
			
			String activitiesString = executor.execute(Request.Get(serviceURI)
				 .addHeader("Authorization", String.format("Bearer %s", token)))
			    .returnContent().asString();

			JsonObject json = new JsonParser().parse(activitiesString).getAsJsonObject();
			JsonArray activitiesList = json.get("list").getAsJsonArray();
//			@SuppressWarnings("rawtypes")
			Iterator<JsonElement> it = activitiesList.iterator();
			JsonArray activities = new JsonArray();
			while ( it.hasNext() ) {
				JsonObject item = it.next().getAsJsonObject();
				String title = item.get("connections").getAsJsonObject().get("plainTitle").getAsString();
				String author = item.get("actor").getAsJsonObject().get("displayName").getAsString();
				String published = item.get("published").getAsString();
				JsonObject activity = new JsonObject();
				activity.addProperty("title", title);
				activity.addProperty("author", author);
				activity.addProperty("publishedDate", published);
				activities.add(activity);
			}
			result.addProperty("type", "activity");
			result.add("data", activities);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
		debug("------> exiting getCommunityActivity with id " + id);
		return result;
	}

	/**
	 * call the Connections API to get details on the authenticated user
	 * @param {String} the oauth access token
	 * @return {JsonObject} containing info on the user
	 */
	private JsonObject getUserIdentity(String token)
	{
		JsonObject result = new JsonObject();
		try
		{
			Executor executor = Executor.newInstance();
			URI serviceURI = 
				new URI("https://" + System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME") 
				+ "/manage/oauth/getUserIdentity").normalize();
			String userIdentityString = executor.execute(Request.Get(serviceURI)
				.addHeader("Authorization", String.format("Bearer %s", token)))
				.returnContent().asString();

			result = new JsonParser().parse(userIdentityString).getAsJsonObject();	
		}
		catch(Exception e)
		{
			debug("error getting user identity: " + e.getMessage());
			e.printStackTrace();
		}
		
		return result;
	}
	
	private void debug(Object o) {
		if (DEBUG) {
			logger.info(o.toString());
		}
	}
}
