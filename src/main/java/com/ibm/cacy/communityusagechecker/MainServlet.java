package com.ibm.cacy.communityusagechecker;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@WebServlet("/api")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static boolean DEBUG = Boolean.parseBoolean(System.getenv("COMMUNITY_USAGE_CHECKER_DEBUG"));
	private static Logger logger = Logger.getLogger(MainServlet.class.getName());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String action = request.getParameter("action");
		
		String token = "";
		if (action != null ) 
		{
			switch (action)
			{
			case "login" :
				debug("in /login");
				String CONNECTIONS_HOST = System.getenv("COMMUNITY_USAGE_CHECKER_HOSTNAME");
				String CC_URL = "https://" + CONNECTIONS_HOST;
				String CC_OAUTH_URL = "/manage/oauth2/authorize";
				String CLIENT_ID = System.getenv("COMMUNITY_USAGE_CHECKER_CLIENT_ID");
				JsonParser parser = new JsonParser();
				JsonObject vcap_application = parser.parse(System.getenv("VCAP_APPLICATION")).getAsJsonObject();
				String APP_HOSTNAME = vcap_application.get("application_uris").getAsJsonArray().get(0).getAsString();

				String redirectURL = CC_URL + CC_OAUTH_URL
					+ "?response_type=code"
					+ "&client_id=" + CLIENT_ID
					+ "&callback_uri=https://" + APP_HOSTNAME + "/api?action=oauthback"
					;

				response.sendRedirect(redirectURL);
				break;
				
			case "oauthback": 
				Authentication auth = new Authentication();
				String code = request.getParameter("code");
				try 
				{
					Map<String, String> map = auth.getAccessToken(code);
					request.getSession(true).setAttribute("community-user-checker-access-token", map.get("access_token"));
					request.getSession(true).setAttribute("community-user-checker-refresh-token", map.get("refresh_token"));
					response.sendRedirect(request.getContextPath() + "/communities.jsp");
				}
				catch(Exception e)
				{
					e.printStackTrace();
					JsonObject error = new JsonObject();
					error.addProperty("error", e.getMessage());
					response.setContentType(MediaType.APPLICATION_JSON);
					response.getWriter().print(error);
				}
				break;
				
			case "getAllCommunities" :
				token = (String)request.getSession().getAttribute("community-user-checker-access-token");
				if (token == null ) 
				{
					JsonObject error = new JsonObject();
					error.addProperty("error", "token");
					response.setContentType(MediaType.APPLICATION_JSON);
					response.getWriter().print(error);
					return;
				}
				boolean showAll = Boolean.parseBoolean(request.getParameter("showAll"));
				JsonObject resultObject = new Community().getAllCommunities(token, showAll);
				response.setContentType(MediaType.APPLICATION_JSON);
				response.getWriter().print(resultObject);
				break;
				
			case "getCommunityDetails" :
				token = (String)request.getSession().getAttribute("community-user-checker-access-token");
				String id = request.getParameter("id");
				if ( id != null && !id.equals("")) {
				JsonObject files = new Community().getCommunityFiles(token, id);
				JsonObject activity = new Community().getCommunityActivity(token, id);
				JsonObject members = new Community().getCommunityMembers(token, id);
				JsonObject subcommunities = new Community().getSubcommunities(token, id);
				JsonArray resultArray = new JsonArray();
				resultArray.add(files);
				resultArray.add(activity);
				resultArray.add(members);
				resultArray.add(subcommunities);
				response.setContentType(MediaType.APPLICATION_JSON);
				response.getWriter().print(resultArray);
				} else {
					JsonObject error = new JsonObject();
					error.addProperty("error", "no Community ID provided");
					response.setContentType(MediaType.APPLICATION_JSON);
					response.getWriter().print(error);
				}
				break;
				
			default :
				response.setContentType(MediaType.TEXT_PLAIN);
				response.getWriter().print("No action provided");
			break;
			} 
		}
		else 
		{
			response.setContentType(MediaType.TEXT_PLAIN);
			response.getWriter().print("No action provided");
		}
	}
	
	private void debug(Object o) {
		if (DEBUG) {
			logger.info(o.toString());
		}
	}
}
