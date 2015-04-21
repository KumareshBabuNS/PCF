package io.pivotal.kr;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.json.JSONTokener;

public class HttpUtils {
	public static boolean isValidVersion(HttpServletRequest req) {
		String version = req.getHeader("X-Broker-API-Version");
		
		if (version == null || version.startsWith("2.") == false) {
			return false;
		}
		
		return true;
	}
	
	public static String getOrganizationGuid(JSONObject obj) throws IOException {
		return obj.getString("organization_guid");
	}
	
	public static String getSpaceGuid(JSONObject obj) throws IOException {
		return obj.getString("space_guid");
	}
	
	public static String getServiceId(JSONObject obj) throws IOException {
		return obj.getString("service_id");
	}
	
	public static String getPlanId(JSONObject obj) throws IOException {
		return obj.getString("plan_id");
	}
	
	public static boolean isValidServiceId(JSONObject obj, String serviceId) throws IOException {
		if (serviceId.equals(obj.getString("service_id")) == false) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isValidPlan(JSONObject obj, String []plans) throws IOException {
		String requestedPlan = obj.getString("plan_id");
		
		boolean isValid = false;
		for (String plan : plans) {
			if (plan.equals(requestedPlan)) {
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	public static JSONObject readBodyAsJson(HttpServletRequest req) throws IOException {
		JSONTokener tokener = new JSONTokener(req.getReader());
		JSONObject jsonObj = new JSONObject(tokener);

		return jsonObj;
	}
}
